package perf.server.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.open.perf.domain.Load;
import com.open.perf.util.FileHelper;
import com.yammer.dropwizard.jersey.params.BooleanParam;
import com.yammer.metrics.annotation.Timed;
import org.apache.log4j.Logger;
import perf.server.client.LoaderAgentClient;
import perf.server.client.MonitoringClient;
import perf.server.config.AgentConfig;
import perf.server.config.JobFSConfig;
import perf.server.config.MonitoringAgentConfig;
import perf.server.daemon.CounterCompoundThread;
import perf.server.daemon.CounterThroughputThread;
import perf.server.daemon.TimerComputationThread;
import perf.server.domain.*;
import perf.server.exception.JobException;
import perf.server.util.DeploymentHelper;
import perf.server.util.ResponseBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static perf.server.domain.JobInfo.JOB_STATUS;

/**
 * Resource that receive Performance Job Request from Client Lib or Loader-Server UI
 */
@Path("/jobs")
public class JobResource {

    private final MonitoringAgentConfig monitoringAgentConfig;
    private AgentConfig agentConfig;
    private JobFSConfig jobFSConfig;

    private static ObjectMapper objectMapper;
    private static Map<String, JobInfo> jobIdInfoMap;
    private static Map<String,Map<String,String>> jobLastResourceMetricInstanceMap;
    private static Logger log;

    static {
        jobIdInfoMap = new HashMap<String, JobInfo>();
        objectMapper = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("MMM dd hh:mm:ss z yyyy");
        objectMapper.setDateFormat(dateFormat);

        jobLastResourceMetricInstanceMap = new HashMap<String, Map<String, String>>();
        log = Logger.getLogger(JobResource.class);
    }

    public JobResource(AgentConfig agentConfig,
                       MonitoringAgentConfig monitoringAgentConfig,
                       JobFSConfig jobFSConfig) {
        this.agentConfig = agentConfig;
        this.monitoringAgentConfig = monitoringAgentConfig;
        this.jobFSConfig = jobFSConfig;
    }
    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl -X POST -d @file-containing-runName http://localhost:9999/loader-server/jobs --header "Content-Type:application/json"
     {"runName" : "runName"}
     * @param jobInfoMap
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Timed
    public JobInfo submitJob(Map jobInfoMap) {
        try {
            return jobSubmitWorkflow(jobInfoMap.get("runName").toString());
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(ResponseBuilder.response(Response.Status.INTERNAL_SERVER_ERROR, e));
        }
    }

    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{jobId}/rerun")
    @POST
    @Timed
    public JobInfo rerunJob(@PathParam("jobId") String oldJobId)
            throws IOException, ExecutionException, InterruptedException, JobException {

        if(!isJobPresent(oldJobId))
            throw new WebApplicationException(ResponseBuilder.jobNotFound(oldJobId));
        if(!isJobOver(oldJobId))
            throw new WebApplicationException(ResponseBuilder.jobNotOver(oldJobId));

        String oldJobJson = getOldJobJson(oldJobId);
        return jobSubmitWorkflow(new ByteArrayInputStream(oldJobJson.getBytes()));
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{jobId}")
    @GET
    @Timed
    public JobInfo getJob(@PathParam("jobId") String jobId) throws IOException {
        jobExistsOrException(jobId);
        return objectMapper.readValue(new File(jobFSConfig.getJobStatusFile(jobId)), JobInfo.class);
    }

    /**
     * Search Job based on runName, jobId and job status
     * By default it would search all running jobs only (And its little slow)
     * @param searchRunName
     * @param searchJobId
     * @param searchJobStatus
     * @return
     */
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    public List<JobInfo> getJobs(@QueryParam("runName") @DefaultValue("") String searchRunName,
                                        @QueryParam("jobId") @DefaultValue("") String searchJobId,
                                        @QueryParam("jobStatus") @DefaultValue("RUNNING")String searchJobStatus) {
        List<JobInfo> jobs = new ArrayList<JobInfo>();
        File runsPath = new File(jobFSConfig.getRunsPath());
        for(File runPath : runsPath.listFiles()) {
            if(runPath.getName().toUpperCase().contains(searchRunName.toUpperCase())) {
                try {
                    BufferedReader runJobsFileReader = FileHelper.bufferedReader(jobFSConfig.getRunJobsFile(runPath.getName()));
                    String runJobId;
                    while((runJobId = runJobsFileReader.readLine()) != null) {
                        if(runJobId.toUpperCase().contains(searchJobId.toUpperCase())) {
                            JobInfo jobInfo = objectMapper.readValue(new File(jobFSConfig.getJobStatusFile(runJobId)), JobInfo.class);
                            if(searchJobStatus.equals("ANY")) {
                                jobs.add(jobInfo);
                            }
                            else if(jobInfo.getJobStatus().toString().equalsIgnoreCase(searchJobStatus)) {
                                jobs.add(jobInfo);
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error(e);
                    throw new WebApplicationException(ResponseBuilder.response(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()));
                }
            }
        }
        return jobs;
    }

    /**
     * Agents Publish Job Load stats on this resource
     * @param request
     * @param jobId
     * @param relatedFilePath
     * @param statsStream
     * @throws IOException
     * @throws InterruptedException
     */
    @Path("/{jobId}/jobStats")
    @POST
    @Timed
    synchronized public void jobStats(@Context HttpServletRequest request,
                                      @PathParam("jobId") String jobId,
                                      @QueryParam("file") String relatedFilePath,
                                      InputStream statsStream)
            throws IOException, InterruptedException {

        // Remove job Id from relative path as loader-server has already created path
        relatedFilePath = relatedFilePath.replace("/"+jobId + "/","");
        String[] relatedFilePathTokens = relatedFilePath.split("\\/");

        // Persist the stats in temporary file. As we have to read and write the stats at two places, same input stream can't be used twice.
        String tmpPath = "/tmp/"+jobId+"-"+System.nanoTime()+".txt";
        FileHelper.persistStream(statsStream, tmpPath, true);

        //TBD Move Following Code to be executed in request queue mode by a daemon thread.
        String[] jobStatsPaths = new String[] {

                jobFSConfig.getJobGroupStatsPath(jobId, relatedFilePathTokens[0])
                        + File.separator + relatedFilePathTokens[1]
                        + File.separator + relatedFilePathTokens[2]
                        + File.separator + "agents"
                        + File.separator + request.getRemoteAddr()
                        + File.separator + "data",

                jobFSConfig.getJobGroupStatsPath(jobId, relatedFilePathTokens[0])
                        + File.separator + relatedFilePathTokens[1]
                        + File.separator + relatedFilePathTokens[2]
                        + File.separator + "agents"
                        + File.separator + "combined"
                        + File.separator + "data",
        };

        // Persisting stats at agent and combined path
        for(String jobStatsPath : jobStatsPaths) {
            FileInputStream fis = new FileInputStream(tmpPath);

            FileHelper.createFilePath(jobStatsPath);
            FileHelper.persistStream(fis, jobStatsPath, true);
            fis.close();
        }

        FileHelper.remove(tmpPath);
    }

    /**
     * Monitoring Agents publishes job Related Monitoring stats here
     * @param request
     * @param jobId
     * @param statsStream
     * @throws IOException
     * @throws InterruptedException
     */
    @Path("/{jobId}/monitoringStats")
    @POST
    @Timed
    public void jobMonitoringStats(@Context HttpServletRequest request,
                                      @PathParam("jobId") String jobId,
                                      InputStream statsStream)
            throws IOException, InterruptedException {
        Map<String,Object> stats = objectMapper.readValue(statsStream, Map.class);
        Map<String,String> resourcesLastInstance = jobLastResourceMetricInstanceMap.get(jobId);
        if(resourcesLastInstance == null)
            resourcesLastInstance = new HashMap<String, String>();

        for(String resource : stats.keySet()) {
            String jobMonitoringStatsPath = jobFSConfig.getJobResourceMonitoringFile(jobId,
                    request.getRemoteAddr(),
                    resource);

            List resourceInstances = (ArrayList) stats.get(resource);
            FileHelper.createFilePath(jobMonitoringStatsPath);

            // Get Last Persisted Metric Instance. Compare it with new one, if changed then persist
            String resourceLastInstance = resourcesLastInstance.get(resource);

            for(int i=0; i<resourceInstances.size(); i++) {
                boolean persistStat = true;
                String resourceNewInstance = resourceInstances.
                        get(i).
                        toString().
                        replace("resourceName="+resource+", ","");

                if(resourceLastInstance != null) {
                    persistStat = !resourceLastInstance.equals(resourceNewInstance);
                }

                if(persistStat) {
                    FileHelper.persistStream(new ByteArrayInputStream((resourceNewInstance+"\n"). // knocking off resource name from the files
                            getBytes()),
                            jobMonitoringStatsPath, true);
                    resourcesLastInstance.put(resource, resourceNewInstance);
                    resourceLastInstance = resourceNewInstance;
                }
            }
            resourcesLastInstance.put(resource, resourceLastInstance);
        }
        jobLastResourceMetricInstanceMap.put(jobId, resourcesLastInstance);
    }

    @Path("/{jobId}/logs")
    @GET
    @Timed
    @Produces(MediaType.TEXT_HTML)
    public String getJobLogs(@PathParam("jobId") String jobId) throws IOException {
        if(!isJobPresent(jobId))
            throw new WebApplicationException(ResponseBuilder.jobNotFound(jobId));

        JobInfo jobInfo = objectMapper.readValue(new File(jobFSConfig.getJobStatusFile(jobId)), JobInfo.class);
        StringBuilder stringBuilder = new StringBuilder();
        for(String agentIp : jobInfo.getAgentsJobStatus().keySet()) {
            stringBuilder.append("<a href=\"" + agentConfig.getJobLogUrl(jobId, agentIp) +"\">" + agentIp + ".log</a><br>");
        }
        return stringBuilder.toString();
    }


    //==================== New Stats APIs starts here

    class MetricStatsMeta {

        private String name;
        private List<String> agents;

        public List<String> getAgents() {
            return agents;
        }

        public void setAgents(List<String> agents) {
            this.agents = agents;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    class GroupStatsMeta {
        private String groupName;
        private List<MetricStatsMeta> timers;
        private List<MetricStatsMeta> counters;

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<MetricStatsMeta> getTimers() {
            return timers;
        }

        public void setTimers(List<MetricStatsMeta> timers) {
            this.timers = timers;
        }

        public List<MetricStatsMeta> getCounters() {
            return counters;
        }

        public void setCounters(List<MetricStatsMeta> counters) {
            this.counters = counters;
        }
    }

    /**
     * Returns Job Stats Meta Data. Useful to see what all stats are being collected as part of performance testing
     * @param jobId
     * @return
     */
    @Path("/{jobId}/jobStats")
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public List<GroupStatsMeta> getJobStats(@PathParam("jobId") String jobId) {
        List<GroupStatsMeta> groups = new ArrayList<GroupStatsMeta>();
        File groupsPath = new File(jobFSConfig.getJobStatsPath(jobId));
        for(File groupPath : groupsPath.listFiles()) {
            GroupStatsMeta groupStatsMeta = new GroupStatsMeta();
            groupStatsMeta.setGroupName(groupPath.getName());


            for(File metricTypeFolder : groupPath.getAbsoluteFile().listFiles()) {
                List<MetricStatsMeta> metrics = new ArrayList<MetricStatsMeta>();
                for(File metricPath : metricTypeFolder.listFiles()) {

                    // I am using timer name here as function name
                    MetricStatsMeta metricStatsMeta = new MetricStatsMeta();
                    metricStatsMeta.setName(metricPath.getName());

                    File agentsPath = new File(metricPath.getAbsoluteFile() + File.separator + "agents");

                    List<String> allAgents = Arrays.asList(agentsPath.list());
                    List<String> agentsHavingData = new ArrayList<String>();

                    for(String agent : allAgents) {
                        if(new File(jobFSConfig.getJobFunctionStatsFile(jobId,
                                groupPath.getName(),
                                metricTypeFolder.getName(),
                                metricPath.getName(),
                                agent)).exists())
                            agentsHavingData.add(agent);
                    }

                    metricStatsMeta.setAgents(agentsHavingData);
                    metrics.add(metricStatsMeta);
                }

                if(metricTypeFolder.getName().equals("timers"))
                    groupStatsMeta.setTimers(metrics);
                if(metricTypeFolder.getName().equals("counters"))
                    groupStatsMeta.setCounters(metrics);
            }
            groups.add(groupStatsMeta);
        }
        return groups;
    }

    /**
     * Returns particular function stats
     * Example /jobId/jobStats/groups/sampleGroup/timers/timer1/agents/127.0.0.1
     * Example /jobId/jobStats/groups/sampleGroup/counters/counter1/agents/127.0.0.1
     * @param jobId
     * @return
     */
    @Path("/{jobId}/jobStats/groups/{groupName}/{metricType}/{metricName}/agents/{agent}")
    @GET
    @Timed
    @Produces(MediaType.TEXT_HTML)
    public InputStream getJobFunctionStats(@PathParam("jobId") String jobId,
                                                    @PathParam("groupName") String groupName,
                                                    @PathParam("metricType") String metricType,
                                                    @PathParam("metricName") String metricName,
                                                    @PathParam("agent") String agent,
                                                    @QueryParam("last") @DefaultValue("false") BooleanParam last) throws FileNotFoundException {
        jobExistsOrException(jobId);
        File statsFile = new File(jobFSConfig.getJobFunctionStatsFile(jobId, groupName, metricType, metricName, agent));
        log.info(statsFile.getAbsolutePath());
        if(!statsFile.exists())
            throw new WebApplicationException(ResponseBuilder.response(Response.Status.NOT_FOUND, String.format("Stats for %s %s Not computed yet",metricType,metricName)));

        if(last.get())
            statsFile = new File(statsFile.getAbsoluteFile()+".last");
        return new FileInputStream(statsFile.getAbsoluteFile());
    }


    class MonitoringAgentStats {
        private String agent;
        private List<String> resources;

        public String getAgent() {
            return agent;
        }

        public void setAgent(String agent) {
            this.agent = agent;
        }

        public List<String> getResources() {
            return resources;
        }

        public void setResources(List<String> resources) {
            this.resources = resources;
        }
    }

    /**
     * Returns Monitoring Stats Meta Data. Useful to see what all monitoring stats are being collected as part of performance testing
     * @param jobId
     * @return
     */
    @Path("/{jobId}/monitoringStats")
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public List<MonitoringAgentStats> getJobMonitoringStats(@PathParam("jobId") String jobId) {
        List<MonitoringAgentStats> monitoringStats = new ArrayList<MonitoringAgentStats>();
        File monitoringResourcesPath = new File(jobFSConfig.getJobMonitoringStatsPath(jobId));
        if(monitoringResourcesPath.exists()) {
            for(File agentPath : new File(monitoringResourcesPath.getAbsolutePath() + File.separator + "agents").listFiles()) {
                MonitoringAgentStats monitoringAgentStats = new MonitoringAgentStats();
                monitoringAgentStats.setAgent(agentPath.getName());
                List<String> resources = new ArrayList<String>();
                for(File resourceFile : new File(agentPath.getAbsolutePath() + File.separator + "Resources").listFiles()) {
                    resources.add(resourceFile.getName().replace(".txt", ""));
                }
                monitoringAgentStats.setResources(resources);
                monitoringStats.add(monitoringAgentStats);
            }
        }
        return monitoringStats;
    }


    /**
     * This resource is called by Loader agent once job is completed
     * @param request
     * @param jobId
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Path("/{jobId}/over")
    @PUT
    @Timed
    public void jobOver(@Context HttpServletRequest request,
                        @PathParam("jobId") String jobId) throws InterruptedException, ExecutionException, IOException {
        String agentIp = request.getRemoteAddr();
        JobInfo jobInfo = jobIdInfoMap.get(jobId);
        jobInfo.jobCompletedInAgent(agentIp);

        if(jobCompleted(jobId)) {
            stopMonitoring(jobId);
            CounterCompoundThread.getCounterCruncherThread().removeJob(jobId);
            CounterThroughputThread.getCounterCruncherThread().removeJob(jobId);
            TimerComputationThread.getComputationThread().removeJob(jobId);
            jobInfo.setEndTime(new Date());
        }
        objectMapper.writeValue(new FileOutputStream(jobFSConfig.getJobStatusFile(jobId)), jobInfo);

    }


    /**
     * Will be called from Loader Server Management UI To kill the job in all agents
     * @param jobId
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Path("/{jobId}/kill")
    @PUT
    @Timed
    public void killJob(@PathParam("jobId") String jobId) throws InterruptedException, ExecutionException, IOException, JobException {
        killJobInAgents(jobId, jobIdInfoMap.get(jobId).getAgentsJobStatus().keySet());
        stopMonitoring(jobId);
        jobIdInfoMap.get(jobId).setEndTime(new Date());
    }

    /**
     * Will be called from Loader Server Management UI To kill the job in specific agents
     * @param jobId
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     * @throws JobException
     */
    @Path("/{jobId}/agents/{agentIps}/kill")
    @PUT
    @Timed
    public void killJob(@PathParam("jobId") String jobId, @PathParam("agentIps") String agentIps) throws InterruptedException, ExecutionException, IOException, JobException {
        killJobInAgents(jobId, Arrays.asList(agentIps.split(",")));
        if(jobCompleted(jobId))
            stopMonitoring(jobId);
    }

    private void runExistsOrException(String runName) {
        if(!new File(jobFSConfig.getRunPath(runName)).exists()) {
            throw new WebApplicationException(ResponseBuilder.runNameDoesNotExist(runName));
        }
    }

    private JobInfo jobSubmitWorkflow(String runName) throws FileNotFoundException {
        runExistsOrException(runName);
        String runFile = jobFSConfig.getRunFile(runName);
        return jobSubmitWorkflow(new FileInputStream(runFile));
    }
    /**
     * Starts monitoring, deploy platform/class libs on agents and then trigger job
     * @param jobJsonStream
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws JobException
     */
    private JobInfo jobSubmitWorkflow(InputStream jobJsonStream) {
        JobInfo jobInfo = null;
        try {

            jobInfo = new JobInfo().
                    setJobId(UUID.randomUUID().toString());

            PerformanceRun performanceRun = objectMapper.readValue(jobJsonStream , PerformanceRun.class);
            jobInfo.setRunName(performanceRun.getRunName());

            // Persisting Job Info(mostly status) in memory
            jobIdInfoMap.put(jobInfo.getJobId(), jobInfo);

            // Persisting Job Json in Local File system.
            persistJob(jobInfo.getJobId(), performanceRun);

            // Raising request to monitoring agents to start collecting metrics from on demand resource collectors
            raiseOnDemandResourceRequest(jobInfo, performanceRun.getOnDemandMetricCollections());

            // Raising request to monitoring agents to start publishing collected metrics to Loader server
            raiseMetricPublishRequest(jobInfo, performanceRun.getMetricCollections());

            // Deploy Libraries on Agents
            deployLibrariesOnAgents(performanceRun.getLoadParts());

            // Submitting Jobs to Loader Agent
            submitJobToAgents(jobInfo, performanceRun.getLoadParts());

            CounterCompoundThread.getCounterCruncherThread().addJob(jobInfo.getJobId());
            CounterThroughputThread.getCounterCruncherThread().addJob(jobInfo.getJobId());
            TimerComputationThread.getComputationThread().addJob(jobInfo.getJobId());

            objectMapper.writeValue(new FileOutputStream(jobFSConfig.getJobStatusFile(jobInfo.getJobId())), jobInfo);
            return jobInfo;
        }
        catch (Exception e) {
            log.error(e);
            jobInfo.setJobStatus(JOB_STATUS.FAILED_TO_START);
            try {
                jobCleanUpOnFailure(jobInfo);
            } catch (Exception e1) {
                log.error(e);
            }

            throw new WebApplicationException(ResponseBuilder.response(Response.Status.INTERNAL_SERVER_ERROR, e));
        }
    }

    /**
     * Clean up Running Job In case Something went wrong while job was being submitted
     * @param jobInfo
     */
    private void jobCleanUpOnFailure(JobInfo jobInfo) throws InterruptedException, ExecutionException, JobException, IOException {
        killJobInAgents(jobInfo.getJobId(), jobInfo.getAgentsJobStatus().keySet());
        stopMonitoring(jobInfo.getJobId());
    }

    /**
     * Get the job json of old Job
     * @param oldJobId
     * @return
     * @throws IOException
     */
    private String getOldJobJson(String oldJobId) throws IOException {
        String jobRunNameFile = jobFSConfig.getJobRunNameFile(oldJobId);
        String runName = FileHelper.readContent(new FileInputStream(jobRunNameFile));
        String oldJobJsonFile = jobFSConfig.getRunFile(runName);
        return FileHelper.readContent(new FileInputStream(oldJobJsonFile));
    }

    private void killJobInAgents(String jobId, Collection<String> agents)
            throws InterruptedException, ExecutionException, JobException, IOException {
        JobInfo jobInfo = jobIdInfoMap.get(jobId);
        if(!jobInfo.getJobStatus().equals(JOB_STATUS.COMPLETED) &&
                !jobInfo.getJobStatus().equals(JOB_STATUS.KILLED)) {

            Map<String, JOB_STATUS> agentsJobStatusMap = jobInfo.getAgentsJobStatus();
            for(String agent : agents) {
                if(!agentsJobStatusMap.get(agent).equals(JOB_STATUS.KILLED) &&
                        !agentsJobStatusMap.get(agent).equals(JOB_STATUS.COMPLETED)) {
                    new LoaderAgentClient(agent, agentConfig.getAgentPort()).killJob(jobId);
                    jobInfo.jobKilledInAgent(agent);
                }
            }
        }
        objectMapper.writeValue(new FileOutputStream(jobFSConfig.getJobStatusFile(jobId)), jobInfo);
    }

    /**
     * Stop monitoring in all agents for the job
     * @param jobId
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void stopMonitoring(String jobId) throws IOException, ExecutionException, InterruptedException {
        jobLastResourceMetricInstanceMap.remove(jobId);
        // Need to get All
        Set<String> monitoringAgentIps = jobIdInfoMap.get(jobId).getMonitoringAgents();

        for(String agentIp : monitoringAgentIps) {
            MonitoringClient monitoringAgent = new MonitoringClient(agentIp, monitoringAgentConfig.getAgentPort());
            monitoringAgent.deleteOnDemandResourceRequest(jobId);
            monitoringAgent.deletePublishResourceRequest(jobId);
        }
    }

    /**
     * If job is completed in all agents it means job is completed
     * @param jobId
     * @return
     */
    private boolean jobCompleted(String jobId) {
        JobInfo jobInfo = jobIdInfoMap.get(jobId);
        return jobInfo.getJobStatus().equals(JobInfo.JOB_STATUS.COMPLETED) ||
                jobInfo.getJobStatus().equals(JobInfo.JOB_STATUS.KILLED);
    }

    /**
     * Raise On Demand Resource Request to Monitoring Agent as Part of Load Job
     * @param jobInfo
     * @param onDemandMetricCollections
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    private void raiseOnDemandResourceRequest(JobInfo jobInfo, List<OnDemandMetricCollection> onDemandMetricCollections) throws InterruptedException, ExecutionException, IOException {
        for(OnDemandMetricCollection onDemandMetricCollection : onDemandMetricCollections) {
            String agentIp = onDemandMetricCollection.getAgent();
            new MonitoringClient(agentIp,
                    monitoringAgentConfig.getAgentPort()).
                    raiseOnDemandResourceRequest(onDemandMetricCollection.buildRequest(jobInfo.getJobId()));
            jobInfo.addMonitoringAgent(agentIp);
            log.info("Request "+onDemandMetricCollection+" raised on Agent "+agentIp);
        }
    }

    /**
     * Raise Metric Publish request to Monitoring Agent
     * @param resourcePublishRequests
     * @param jobInfo
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void raiseMetricPublishRequest(ArrayNode resourcePublishRequests, JobInfo jobInfo) throws IOException, ExecutionException, InterruptedException {
        for(int i=0; i<resourcePublishRequests.size(); i++) {
            ObjectNode requestPart = (ObjectNode) resourcePublishRequests.get(i);

            String agentIp = requestPart.get("agent").textValue();
            new MonitoringClient(agentIp,
                    monitoringAgentConfig.getAgentPort()).
                    raiseMetricPublishRequest(objectMapper.readValue(requestPart.get("request").
                            toString().
                            replace("{jobId}", jobInfo.getJobId()),
                            MetricPublisherRequest.class).
                            setRequestId(jobInfo.getJobId()));
            jobInfo.addMonitoringAgent(agentIp);
            log.info("Request "+requestPart.get("request").toString()+" raised on Agent "+agentIp);
        }
    }

    /**
     * Raise Metric Publish request to Monitoring Agent
     * @param jobInfo
     * @param metricCollections
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    private void raiseMetricPublishRequest(JobInfo jobInfo, List<MetricCollection> metricCollections) throws InterruptedException, ExecutionException, IOException {
        for(MetricCollection metricCollection : metricCollections) {
            String agentIp = metricCollection.getAgent();
            new MonitoringClient(agentIp,
                    monitoringAgentConfig.getAgentPort()).
                    raiseMetricPublishRequest(metricCollection.buildRequest(jobInfo.getJobId()));
            jobInfo.addMonitoringAgent(agentIp);
            log.info("Request "+metricCollection+" raised on Agent "+agentIp);
        }
    }

    /**
     * Submitting Load Job To Loader Agents
     * @param jobInfo
     * @param loadParts
     * @throws IOException
     * @throws JobException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void submitJobToAgents(JobInfo jobInfo, List<LoadPart> loadParts)
            throws IOException, JobException, ExecutionException, InterruptedException {
        jobInfo.setStartTime(new Date());
        for(LoadPart loadPart : loadParts) {
            // Submitting Job To Agent
            List<String> agentIps = loadPart.getAgents();
            List<String> classes = loadPart.getClasses();

            StringBuilder classListWithNewLine = new StringBuilder();
            for(String clazz : classes)
                classListWithNewLine.append(clazz+"\n");

            for(String agentIp : agentIps) {
                submitJobToAgent(agentIp,
                        jobInfo.getJobId(),
                        loadPart.getLoad(),
                        classListWithNewLine.toString());
                jobInfo.jobRunningInAgent(agentIp);
            }
        }
    }

    /**
     * Deploy Platform Libs and Class Libs on Loader agents If Required
     * @param loadParts
     * @throws IOException
     * @throws JobException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void deployLibrariesOnAgents(List<LoadPart> loadParts)
            throws IOException, JobException, ExecutionException, InterruptedException {
        for(LoadPart loadPart : loadParts) {
            // Submitting Job To Agent
            List<String> agentIps = loadPart.getAgents();
            List<String> classes = loadPart.getClasses();

            StringBuilder classListWithNewLine = new StringBuilder();
            for(String clazz : classes)
                classListWithNewLine.append(clazz+"\n");

            for(String agentIp : agentIps) {
                DeploymentHelper.instance().deployPlatformLibsOnAgent(agentIp);
                DeploymentHelper.instance().deployClassLibsOnAgent(agentIp, classListWithNewLine.toString().trim());
            }
        }
    }

    /**
     * Submitting Job To Loader Agent
     * @param agentIp
     * @param jobId
     * @param load
     * @throws IOException
     * @throws JobException
     */
    private void submitJobToAgent(String agentIp, String jobId, Load load, String classListStr)
            throws InterruptedException, ExecutionException, JobException, IOException {
        log.info("Agent Ip :" + agentIp);
        new LoaderAgentClient(agentIp,
                agentConfig.getAgentPort()).
                submitJob(jobId, load, classListStr);
        log.info("Load Job " + load + " submitted to Agent " + agentIp);
    }

    /**
     * Persist Job Details in FS
     * @param jobId
     * @param performanceRun
     * @throws IOException
     */
    private void persistJob(String jobId, PerformanceRun performanceRun) throws IOException {
        String runName = performanceRun.getRunName();

        // Persisting run details
        String runFile = jobFSConfig.getRunFile(runName);
        FileHelper.createFilePath(runFile);
        objectMapper.writeValue(new FileOutputStream(runFile), performanceRun);

        // Add file containing run name in job folder
        String jobRunNameFile = jobFSConfig.getJobRunNameFile(jobId);
        FileHelper.createFilePath(jobRunNameFile);
        FileHelper.persistStream(new ByteArrayInputStream(runName.getBytes()),
                jobRunNameFile,
                false);

        // Adding job ids in run folder file
        String runJobsFile = jobFSConfig.getRunJobsFile(runName);
        FileHelper.createFilePath(runJobsFile);
        FileHelper.persistStream(new ByteArrayInputStream((jobId + "\n").getBytes()),
                runJobsFile,
                true);
    }

    private boolean isJobPresent(String jobId) {
        return new File(jobFSConfig.getJobsPath() + "/" + jobId).exists();
    }

    private boolean isJobOver(String jobId) {
        return !jobIdInfoMap.containsKey(jobId) ||
                (jobIdInfoMap.get(jobId).getJobStatus().equals(JOB_STATUS.KILLED) ||
                jobIdInfoMap.get(jobId).getJobStatus().equals(JOB_STATUS.COMPLETED));
    }

    private void jobExistsOrException(String jobId) {
        if(!new File(jobFSConfig.getJobPath(jobId)).exists()) {
            throw new WebApplicationException(ResponseBuilder.resourceNotFound("Job", jobId));
        }
    }

    public static void main(String[] args) {
        String statsPath = "/d7030544-d3b1-4265-869c-fdcdd3427b7c/defaultGroup_HttpGet_perf.operation.http.function.HttpGet/counters/HttpGet_with_perf.operation.http.function.HttpGet_count";
        StringTokenizer tokenizer = new StringTokenizer(statsPath,"\\/");
        for(;tokenizer.hasMoreElements();)
            System.out.println(tokenizer.nextElement());
    }
}