package perf.server.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.metrics.annotation.Timed;
import org.apache.log4j.Logger;
import perf.server.client.LoaderAgentClient;
import perf.server.client.MonitoringClient;
import perf.server.config.AgentConfig;
import perf.server.config.JobStatsConfig;
import perf.server.config.MonitoringAgentConfig;
import perf.server.domain.JobInfo;
import perf.server.domain.MetricPublisherRequest;
import perf.server.domain.OnDemandCollectorRequest;
import perf.server.exception.JobException;
import perf.server.util.FileHelper;
import perf.server.util.ResponseBuilderHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import static perf.server.domain.JobInfo.JOB_STATUS;

@Path("/jobs")
public class JobResource {

    private final MonitoringAgentConfig monitoringAgentConfig;
    private AgentConfig agentConfig;
    private JobStatsConfig jobStatsConfig;

    private static ObjectMapper mapper;
    private static Map<String, JobInfo> jobIdInfoMap;
    private static Map<String,Map<String,String>> jobLastResourceMetricInstanceMap;
    private static Logger log;

    static {
        jobIdInfoMap = new HashMap<String, JobInfo>();
        mapper = new ObjectMapper();
        jobLastResourceMetricInstanceMap = new HashMap<String, Map<String, String>>();
        log = Logger.getLogger(JobResource.class);
    }

    public JobResource(AgentConfig agentConfig,
                       MonitoringAgentConfig monitoringAgentConfig,
                       JobStatsConfig jobStatsConfig) {
        this.agentConfig = agentConfig;
        this.monitoringAgentConfig = monitoringAgentConfig;
        this.jobStatsConfig = jobStatsConfig;
    }
    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl
     -X POST
     -H "Content-Type: multipart/form-data"
     -F "jobJson=@Path-To-File-Containing-Job-Json"
     -F "classList=@Path-To-File-Containing-Classes-Separated-With-New-Line"
     http://localhost:9999/loader-server/jobs
     * @param jobJsonInfoStream
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Timed
    public JobInfo submitJob(@FormDataParam("jobJson") InputStream jobJsonInfoStream)
            throws IOException, ExecutionException, InterruptedException, JobException {
        return jobSubmitWorkflow(jobJsonInfoStream);
    }

    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{jobId}/rerun")
    @POST
    @Timed
    public JobInfo rerunJob(@PathParam("jobId") String oldJobId)
            throws IOException, ExecutionException, InterruptedException, JobException {

        if(!isJobPresent(oldJobId))
            throw new WebApplicationException(ResponseBuilderHelper.jobNotFound(oldJobId));
        if(!isJobOver(oldJobId))
            throw new WebApplicationException(ResponseBuilderHelper.jobNotOver(oldJobId));

        String oldJobJson = getOldJobJson(oldJobId);
        return jobSubmitWorkflow(new ByteArrayInputStream(oldJobJson.getBytes()));
    }

    private boolean isJobPresent(String jobId) {
        return new File(jobStatsConfig.getRunJobMappingFile().replace("{jobId}", jobId)).exists();
    }

    private boolean isJobOver(String jobId) {
        return !jobIdInfoMap.containsKey(jobId) ||
                (jobIdInfoMap.get(jobId).getJobStatus().equals(JOB_STATUS.KILLED) ||
                jobIdInfoMap.get(jobId).getJobStatus().equals(JOB_STATUS.COMPLETED));
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{jobId}")
    @GET
    @Timed
    public JobInfo getJob(@PathParam("jobId") String jobId) {
        return jobIdInfoMap.get(jobId);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    public Map getJobs() {
        return jobIdInfoMap;
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
        String jobStatsPath = jobStatsConfig.getJobStatsFolder().
                replace("{jobId}", jobId).
                replace("{agentIp}", request.getRemoteAddr());

        String statFilePath = jobStatsPath +
                File.separator +
                relatedFilePath.replace("/"+jobId,"");

        FileHelper.createFilePath(statFilePath);
        FileHelper.persistStream(statsStream, statFilePath, true);
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
        Map<String,Object> stats = mapper.readValue(statsStream, Map.class);
        Map<String,String> resourcesLastInstance = jobLastResourceMetricInstanceMap.get(jobId);
        if(resourcesLastInstance == null)
            resourcesLastInstance = new HashMap<String, String>();

        for(String resource : stats.keySet()) {
            String jobMonitoringStatsPath = jobStatsConfig.getJobMonitoringStats().
                    replace("{jobId}", jobId).
                    replace("{agentIp}", request.getRemoteAddr()).
                    replace("{resource}", resource);

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
        jobIdInfoMap.get(jobId).jobCompletedInAgent(agentIp);

        if(jobCompleted(jobId))
            stopMonitoring(jobId);
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

    private JobInfo jobSubmitWorkflow(InputStream jobJsonStream)
            throws IOException, ExecutionException, InterruptedException, JobException {
        JobInfo jobInfo = new JobInfo().
                setJobId(UUID.randomUUID().toString());
        JsonNode jobInfoJsonNode = mapper.readValue(jobJsonStream,JsonNode.class);

        // Persisting Job Json in Local File system.
        persistJob(jobInfo.getJobId(), jobInfoJsonNode);

        // Raising request to monitoring agents to start collecting metrics from on demand resource collectors
        raiseOnDemandResourceRequest((ArrayNode) jobInfoJsonNode.get("onDemandResourcesRequests"), jobInfo);

        // Raising request to monitoring agents to start publishing collected metrics to Loader server
        raiseMetricPublishRequest((ArrayNode) jobInfoJsonNode.get("resourcePublishRequests"), jobInfo);

        // Submitting Jobs to Loader Agent
        submitJobToAgents((ArrayNode) jobInfoJsonNode.get("jobs"), jobInfo);

        // Persisting Job Info(mostly status) in memory
        jobIdInfoMap.put(jobInfo.getJobId(), jobInfo);

        return jobInfo;
    }

    /**
     * Get the job json of old Job
     * @param oldJobId
     * @return
     * @throws IOException
     */
    private String getOldJobJson(String oldJobId) throws IOException {
        InputStream is = new FileInputStream(jobStatsConfig.getRunJobMappingFile().replace("{jobId}", oldJobId));
        try {
            return FileHelper.readContent(is);
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        }
        finally {
            if(is != null)
                is.close();
        }
    }

    private void killJobInAgents(String jobId, Collection<String> agents) throws InterruptedException, ExecutionException, JobException {
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
     * @param onDemandResourcesRequests
     * @param jobInfo
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void raiseOnDemandResourceRequest(ArrayNode onDemandResourcesRequests, JobInfo jobInfo)
            throws IOException, ExecutionException, InterruptedException {

        for(int i=0; i<onDemandResourcesRequests.size(); i++) {
            ObjectNode requestPart = (ObjectNode) onDemandResourcesRequests.get(i);

            String agentIp = requestPart.get("agent").textValue();
            new MonitoringClient(agentIp,
                    monitoringAgentConfig.getAgentPort()).
                    raiseOnDemandResourceRequest(new OnDemandCollectorRequest().
                            setRequestId(jobInfo.getJobId()).
                            setCollectors(mapper.readValue(requestPart.get("collectors").toString(), List.class)));
            jobInfo.addMonitoringAgent(agentIp);
            log.info("Request "+requestPart.get("collectors").toString()+" raised on Agent "+agentIp);
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
                    raiseMetricPublishRequest(mapper.readValue(requestPart.get("request").
                            toString().
                            replace("{jobId}",jobInfo.getJobId()),
                            MetricPublisherRequest.class).
                            setRequestId(jobInfo.getJobId()));
            jobInfo.addMonitoringAgent(agentIp);
            log.info("Request "+requestPart.get("request").toString()+" raised on Agent "+agentIp);
        }
    }

    /**
     * Submitting Job To Loader Agent
     * @param jobs
     * @param jobInfo
     * @throws IOException
     * @throws JobException
     */
    private void submitJobToAgents(ArrayNode jobs, JobInfo jobInfo) throws IOException, JobException {
        for(int i=0; i<jobs.size(); i++) {
            ObjectNode jobPart = (ObjectNode) jobs.get(i);
            try {
                // Submitting Job To Agent
                String agentIp = jobPart.get("agent").textValue();
                String operationClassListStr = jobPart.get("classList").toString();
                new LoaderAgentClient(agentIp,
                        agentConfig.getAgentPort()).
                        submitJob(jobInfo.getJobId(), jobPart.get("jobPartInfo").toString(), operationClassListStr);
                jobInfo.jobRunningInAgent(agentIp);
                log.info("Load Job "+jobPart.get("jobPartInfo").toString()+" submitted to Agent "+agentIp);
            }  catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ExecutionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void persistJob(String jobId, JsonNode jobInfoJsonNode) throws IOException {
        String runName = jobInfoJsonNode.get("runName").textValue();
        String runFile = jobStatsConfig.getRunFile().
                replace("{runName}", runName);
        FileHelper.createFilePath(runFile);
        FileHelper.persistStream(new ByteArrayInputStream(jobInfoJsonNode.toString().getBytes()),
                runFile,
                false);

        String runJobMappingFile = jobStatsConfig.getRunJobMappingFile().
                replace("{runName}", runName);
        FileHelper.createFilePath(runJobMappingFile);
        FileHelper.persistStream(new ByteArrayInputStream((jobId+"\n").toString().getBytes()),
                runJobMappingFile,
                true);

        String jobRunNameFile = jobStatsConfig.getJobRunNameFile().
                replace("{jobId}", jobId);
        FileHelper.createFilePath(runJobMappingFile);
        FileHelper.persistStream(new ByteArrayInputStream(runName.getBytes()),
                jobRunNameFile,
                false);
    }
}