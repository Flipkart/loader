package perf.server.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.open.perf.domain.Load;
import com.open.perf.jackson.ObjectMapperUtil;
import com.open.perf.util.FileHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.server.cache.AgentsCache;
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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Helps in doing operations around Job
 */
public class JobHelper {

    private LoadingCache<String, Job> jobs = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(
                    new CacheLoader<String, Job>() {
                        public Job load(String jobId) throws IOException {
                            return objectMapper.readValue(new File(jobFSConfig.getJobStatusFile(jobId)), Job.class);
                        }
                    });

    private List<String> cachedJobIds;
    private JobFSConfig jobFSConfig;
    private AgentConfig agentConfig;
    private MonitoringAgentConfig monitoringAgentConfig;

    private static JobHelper myInstance;
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();
    private static Logger log = LoggerFactory.getLogger(JobHelper.class);


    public JobHelper(JobFSConfig jobFSConfig, AgentConfig agentConfig, MonitoringAgentConfig monitoringAgentConfig) {
        this.jobFSConfig = jobFSConfig;
        this.agentConfig = agentConfig;
        this.monitoringAgentConfig = monitoringAgentConfig;
        this.cachedJobIds = new ArrayList<String>();
    }

    public static JobHelper build(JobFSConfig jobFSConfig, AgentConfig agentConfig, MonitoringAgentConfig monitoringAgentConfig) {
        if(myInstance == null)
            myInstance = new JobHelper(jobFSConfig, agentConfig, monitoringAgentConfig);
        return myInstance;
    }

    public static JobHelper instance() {
        return myInstance;
    }

    public void submitJob(Job job, List<LoaderAgent> freeAgents) {
        String runFile = jobFSConfig.getRunFile(job.getRunName());
        List<LoaderAgent> agentsToUse = new ArrayList<LoaderAgent>();
        for(LoaderAgent freeAgent : freeAgents) {
            agentsToUse.add(freeAgent);
        }

        try {
            PerformanceRun performanceRun = objectMapper.readValue(new File(runFile) , PerformanceRun.class);
            job.setRunName(performanceRun.getRunName());

            // Persisting Job Json in Local File system.
            persistJobAndRunInfo(job.getJobId(), performanceRun);

            // Raising request to monitoring agents to start collecting metrics from on demand resource collectors
            raiseOnDemandResourceRequest(job, performanceRun.getOnDemandMetricCollections());

            // Raising request to monitoring agents to start publishing collected metrics to Loader server
            raiseMetricPublishRequest(job, performanceRun.getMetricCollections());

            // Deploy Libraries on Agents
            deployLibrariesOnAgents(performanceRun.getLoadParts(), agentsToUse);

            // Submitting Jobs to Loader Agent
            submitJobToAgents(job, performanceRun.getLoadParts(), agentsToUse);

            CounterCompoundThread.getCounterCruncherThread().addJob(job.getJobId());
            CounterThroughputThread.getCounterCruncherThread().addJob(job.getJobId());
            TimerComputationThread.getComputationThread().addJob(job.getJobId());

            persistJob(job);
        }
        catch (Exception e) {
            log.error("Job Submission Failed",e);
            job.setJobStatus(Job.JOB_STATUS.FAILED_TO_START);
            try {
                jobCleanUpOnFailure(job);
            } catch (Exception e1) {
                log.error("Job Clean up Failure",e);
            }
        }
    }

    /**
     * Clean up Running Job In case Something went wrong while job was being submitted
     * @param job
     */
    private void jobCleanUpOnFailure(Job job) throws InterruptedException, ExecutionException, JobException, IOException {
        killJobInAgents(job, job.getAgentsJobStatus().keySet());
        stopMonitoring(job);
    }

    /**
     * Persist Job Details in FS
     * @param jobId
     * @param performanceRun
     * @throws java.io.IOException
     */
    private void persistJobAndRunInfo(String jobId, PerformanceRun performanceRun) throws IOException {
        String runName = performanceRun.getRunName();

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

    /**
     * Raise On Demand Resource Request to Monitoring Agent as Part of Load Job
     * @param job
     * @param onDemandMetricCollections
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     * @throws IOException
     */
    private void raiseOnDemandResourceRequest(Job job, List<OnDemandMetricCollection> onDemandMetricCollections) throws InterruptedException, ExecutionException, IOException {
        for(OnDemandMetricCollection onDemandMetricCollection : onDemandMetricCollections) {
            String agentIp = onDemandMetricCollection.getAgent();
            new MonitoringClient(agentIp,
                    monitoringAgentConfig.getAgentPort()).
                    raiseOnDemandResourceRequest(onDemandMetricCollection.buildRequest(job.getJobId()));
            job.addMonitoringAgent(agentIp);
            log.info("Request "+onDemandMetricCollection+" raised on Agent "+agentIp);
        }
    }

    /**
     * Raise Metric Publish request to Monitoring Agent
     * @param job
     * @param metricCollections
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    private void raiseMetricPublishRequest(Job job, List<MetricCollection> metricCollections) throws InterruptedException, ExecutionException, IOException {
        for(MetricCollection metricCollection : metricCollections) {
            String agentIp = metricCollection.getAgent();
            new MonitoringClient(agentIp,
                    monitoringAgentConfig.getAgentPort()).
                    raiseMetricPublishRequest(metricCollection.buildRequest(job.getJobId()));
            job.addMonitoringAgent(agentIp);
            log.info("Request "+metricCollection+" raised on Agent "+agentIp);
        }
    }

    /**
     * Submitting Load Job To Loader Agents
     *
     * @param job
     * @param loadParts
     * @param agentsToUse
     * @throws IOException
     * @throws perf.server.exception.JobException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void submitJobToAgents(Job job, List<LoadPart> loadParts, List<LoaderAgent> agentsToUse)
            throws IOException, JobException, ExecutionException, InterruptedException {
        job.setStartTime(new Date());
        for(LoadPart loadPart : loadParts) {
            // Submitting Job To Agent
            List<String> classes = loadPart.getClasses();

            StringBuilder classListWithNewLine = new StringBuilder();
            for(String clazz : classes)
                classListWithNewLine.append(clazz+"\n");

            for(int agentI = 1; agentI<=loadPart.getAgents(); agentI++) {
                LoaderAgent agent = agentsToUse.remove(0);
                submitJobToAgent(agent.getIp(),
                        job.getJobId(),
                        loadPart.getLoad(),
                        classListWithNewLine.toString());
                job.jobRunningInAgent(agent.getIp());
                AgentsCache.getAgentInfo(agent.getIp()).addRunningJob(job.getJobId());
                AgentsCache.getAgentInfo(agent.getIp()).setBusy();
            }
        }
    }

    /**
     * Deploy Platform Libs and Class Libs on Loader agents If Required
     *
     * @param loadParts
     * @param agentsToUse
     * @throws IOException
     * @throws JobException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void deployLibrariesOnAgents(List<LoadPart> loadParts, List<LoaderAgent> agentsToUse)
            throws IOException, JobException, ExecutionException, InterruptedException {
        for(LoadPart loadPart : loadParts) {
            List<String> classes = loadPart.getClasses();

            StringBuilder classListWithNewLine = new StringBuilder();
            for(String clazz : classes)
                classListWithNewLine.append(clazz+"\n");

            for(LoaderAgent agent : agentsToUse) {
                DeploymentHelper.instance().deployPlatformLibsOnAgent(agent.getIp());
                DeploymentHelper.instance().deployClassLibsOnAgent(agent.getIp(), classListWithNewLine.toString().trim());
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

    public void killJobInAgents(Job job, Collection<String> agents)
            throws InterruptedException, ExecutionException, JobException, IOException {
        String jobId = job.getJobId();
        if(!job.getJobStatus().equals(Job.JOB_STATUS.COMPLETED) &&
                !job.getJobStatus().equals(Job.JOB_STATUS.KILLED)) {

            Map<String, Job.JOB_STATUS> agentsJobStatusMap = job.getAgentsJobStatus();
            for(String agent : agents) {
                if(!agentsJobStatusMap.get(agent).equals(Job.JOB_STATUS.KILLED) &&
                        !agentsJobStatusMap.get(agent).equals(Job.JOB_STATUS.COMPLETED)) {
                    new LoaderAgentClient(agent, agentConfig.getAgentPort()).killJob(jobId);
                    job.jobKilledInAgent(agent);
                }
            }
        }
        persistJob(job);
    }

    /**
     * Stop monitoring in all agents for the job
     * @param job
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void stopMonitoring(Job job) throws IOException, ExecutionException, InterruptedException {
//        jobLastResourceMetricInstanceMap.remove(jobId);

        // Need to get All
        Set<String> monitoringAgentIps = job.getMonitoringAgents();
        String jobId = job.getJobId();
        for(String agentIp : monitoringAgentIps) {
            MonitoringClient monitoringAgent = new MonitoringClient(agentIp, monitoringAgentConfig.getAgentPort());
            monitoringAgent.deleteOnDemandResourceRequest(jobId);
            monitoringAgent.deletePublishResourceRequest(jobId);
        }
    }

    /**
     * Search Jobs in File System
     * @param searchJobId
     * @param searchRunName
     * @param searchJobStatus
     * @return
     */
    public List<Job> searchJobs(String searchJobId, String searchRunName, String searchJobStatus) throws IOException {
        List<Job> jobs = new ArrayList<Job>();
        File runsPath = new File(jobFSConfig.getRunsPath());
        for(File runPath : runsPath.listFiles()) {
            if(runPath.getName().toUpperCase().contains(searchRunName.toUpperCase())) {
                File runJobsFile = new File(jobFSConfig.getRunJobsFile(runPath.getName()));
                if(runJobsFile.exists()) {
                    BufferedReader runJobsFileReader = FileHelper.bufferedReader(runJobsFile.getAbsolutePath());
                    String runJobId;
                    while((runJobId = runJobsFileReader.readLine()) != null) {
                        if(runJobId.toUpperCase().contains(searchJobId.toUpperCase())) {
                            Job job = objectMapper.readValue(new File(jobFSConfig.getJobStatusFile(runJobId)), Job.class);
                            if(searchJobStatus.equals("ANY")) {
                                jobs.add(job);
                            }
                            else if(job.getJobStatus().toString().equalsIgnoreCase(searchJobStatus)) {
                                jobs.add(job);
                            }
                        }
                    }
                }
            }
        }
        return jobs;
    }

    public void persistJobStatsComingFromAgent(String jobId, String agentIp, String relatedFilePath, InputStream jobStatsStream) throws IOException {
                // Remove job Id from relative path as loader-server has already created path
        relatedFilePath = relatedFilePath.replace("/"+jobId + "/","");
        String[] relatedFilePathTokens = relatedFilePath.split("\\/");

        // Persist the stats in temporary file. As we have to read and write the stats at two places, same input stream can't be used twice.
        String tmpPath = "/tmp/"+jobId+"-"+System.nanoTime()+".txt";
        FileHelper.persistStream(jobStatsStream, tmpPath, true);

        //TBD Move Following Code to be executed in request queue mode by a daemon thread.
        String[] jobStatsPaths = new String[] {

                jobFSConfig.getJobGroupStatsPath(jobId, relatedFilePathTokens[0])
                        + File.separator + relatedFilePathTokens[1]
                        + File.separator + relatedFilePathTokens[2]
                        + File.separator + "agents"
                        + File.separator + agentIp
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

    public static class MetricStatsMeta {

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

    public static class GroupStatsMeta {
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
     * Get Meta information about all the metrics present for a job
     * @param jobId
     * @return
     */
    public List<GroupStatsMeta> getJobMetricsStatsMeta(String jobId) {
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
     * Get Stats for specific metric for a job
     * @param jobId
     * @param groupName
     * @param metricType
     * @param metricName
     * @param agent
     * @param last
     * @return
     * @throws FileNotFoundException
     */
    public InputStream getJobMetricStats(String jobId, String groupName, String metricType, String metricName, String agent, boolean last) throws FileNotFoundException {
        File statsFile = new File(jobFSConfig.getJobFunctionStatsFile(jobId, groupName, metricType, metricName, agent));
        log.info(statsFile.getAbsolutePath());
        if(!statsFile.exists())
            throw new WebApplicationException(ResponseBuilder.response(Response.Status.NOT_FOUND, String.format("Stats for %s %s Not computed yet",metricType,metricName)));

        if(last)
            statsFile = new File(statsFile.getAbsoluteFile()+".last");
        return new FileInputStream(statsFile.getAbsoluteFile());

    }

    public static class MonitoringAgentStats {
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
     * Get Meta Information about all the monitoring metrics stored for a Job
     * @param jobId
     * @return
     */
    public List<MonitoringAgentStats> getJobMonitoringStatsMeta(String jobId) {
        List<MonitoringAgentStats> monitoringStats = new ArrayList<MonitoringAgentStats>();
        File monitoringResourcesPath = new File(jobFSConfig.getJobMonitoringStatsPath(jobId));
        if(monitoringResourcesPath.exists()) {
            for(File agentPath : new File(monitoringResourcesPath.getAbsolutePath() + File.separator + "agents").listFiles()) {
                MonitoringAgentStats monitoringAgentStats = new MonitoringAgentStats();
                monitoringAgentStats.setAgent(agentPath.getName());
                List<String> resources = new ArrayList<String>();
                for(File resourceFile : new File(agentPath.getAbsolutePath() + File.separator + "Resources").listFiles()) {
                    if(resourceFile.getName().endsWith(".txt"))
                        resources.add(resourceFile.getName().replace(".txt", ""));
                }
                monitoringAgentStats.setResources(resources);
                monitoringStats.add(monitoringAgentStats);
            }
        }
        return monitoringStats;

    }

    /**
     * Get collected resource stats that is being monitored for a job
     * @param jobId
     * @param agent
     * @param resourceName
     * @param last
     * @return
     * @throws FileNotFoundException
     */
    public InputStream getJobMonitoringResourceStats(String jobId, String agent, String resourceName, Boolean last) throws FileNotFoundException {
        File statsFile = new File(jobFSConfig.getJobResourceMonitoringFile(jobId, agent, resourceName));
        log.info(statsFile.getAbsolutePath());
        if(!statsFile.exists())
            throw new WebApplicationException(ResponseBuilder.response(Response.Status.NOT_FOUND, String.format("Monitoring Stats for %s %s Not collected yet",agent,resourceName)));

        if(last)
            statsFile = new File(statsFile.getAbsoluteFile()+".last");
        return new FileInputStream(statsFile.getAbsoluteFile());

    }

    /**
     * Returns metric key value collected for a Resource
     * @param jobId
     * @param agent
     * @param resourceName
     * @return
     */
    public Set<String> getJobMonitoringResourceMeta(String jobId, String agent, String resourceName) throws IOException {
        File statsFile = new File(jobFSConfig.getJobResourceMonitoringFile(jobId, agent, resourceName));
        log.info(statsFile.getAbsolutePath());
        if(!statsFile.exists())
            throw new WebApplicationException(ResponseBuilder.response(Response.Status.NOT_FOUND, String.format("Monitoring Stats for %s %s Not collected yet",agent,resourceName)));

        Map<String, Object> resourceLastInstance = objectMapper.readValue(new File(statsFile.getAbsolutePath()+".last"), Map.class);
        Map<String, Object> metricValueMap = (Map<String, Object>) resourceLastInstance.get("metrics");
        return metricValueMap.keySet();
    }

    /**
     * This function really doesn't belong here. Just added here for the time being.
     * @param runName
     * @return
     */
    public PerformanceRun getPerformanceRun(String runName) throws IOException {
        return objectMapper.readValue(new File(jobFSConfig.getRunFile(runName)), PerformanceRun.class);
    }


    public Job jobExistsOrException(String jobId) throws IOException, ExecutionException {
        if(!new File(jobFSConfig.getJobPath(jobId)).exists()) {
            throw new WebApplicationException(ResponseBuilder.resourceNotFound("Job", jobId));
        }
        return jobs.get(jobId);
    }

    /**
     * Persist Job Status in File System
     * @param job
     * @throws IOException
     */
    public void persistJob(Job job) throws IOException {
        objectMapper.writeValue(new FileOutputStream(jobFSConfig.getJobStatusFile(job.getJobId())), job);
    }
}