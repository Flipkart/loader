package perf.server.domain;

import com.open.perf.domain.Load;
import com.open.perf.jackson.ObjectMapperUtil;
import com.open.perf.util.FileHelper;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.server.cache.AgentsCache;
import perf.server.client.LoaderAgentClient;
import perf.server.client.MonitoringClient;
import perf.server.config.LoaderServerConfiguration;
import perf.server.daemon.CounterCompoundThread;
import perf.server.daemon.CounterThroughputThread;
import perf.server.daemon.TimerComputationThread;
import perf.server.exception.JobException;
import perf.server.util.DeploymentHelper;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Job {

    private static final LoaderServerConfiguration configuration = LoaderServerConfiguration.instance();
    private static final ObjectMapper objectMapper = ObjectMapperUtil.instance();
    private static Logger log = LoggerFactory.getLogger(Job.class);

    public static enum JOB_STATUS {
        QUEUED, RUNNING, PAUSED, COMPLETED, KILLED, FAILED_TO_START;
    }

    private String jobId;
    private String runName;
    private Date startTime, endTime;
    private JOB_STATUS jobStatus;
    private Map<String,AgentJobStatus> agentsJobStatus;
    private Set<String> monitoringAgents;

    public static class AgentJobStatus {
        private String agentIp;
        private boolean inStress;
        private JOB_STATUS job_status;
        private Map<String, Object> healthStatus;

        public String getAgentIp() {
            return agentIp;
        }

        public AgentJobStatus setAgentIp(String agentIp) {
            this.agentIp = agentIp;
            return this;
        }

        public JOB_STATUS getJob_status() {
            return job_status;
        }

        public AgentJobStatus setJob_status(JOB_STATUS job_status) {
            this.job_status = job_status;
            return this;
        }

        public Map<String, Object> getHealthStatus() {
            return healthStatus;
        }

        public AgentJobStatus setHealthStatus(Map<String, Object> healthStatus) {
            if(healthStatus != null) {
                this.healthStatus = healthStatus;
                this.inStress = (Boolean)this.healthStatus.remove("inStress");
            }
            return this;
        }

        public boolean isInStress() {
            return inStress;
        }

        public AgentJobStatus setInStress(boolean inStress) {
            this.inStress = inStress;
            return this;
        }
    }

    public Job() {
        this.jobStatus = JOB_STATUS.QUEUED;
        this.monitoringAgents = new HashSet<String>();
        this.agentsJobStatus = new LinkedHashMap<String, AgentJobStatus>();
    }

    // All Getters and Setters
    public String getJobId() {
        return jobId;
    }

    public Job setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    public String getRunName() {
        return runName;
    }

    public Job setRunName(String runName) {
        this.runName = runName;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Job setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Job setEndTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }

    public JOB_STATUS getJobStatus() {
        return jobStatus;
    }

    public Job setJobStatus(JOB_STATUS jobStatus) {
        this.jobStatus = jobStatus;
        return this;
    }

    public Set<String> getMonitoringAgents() {
        return monitoringAgents;
    }

    public void setMonitoringAgents(Set<String> monitoringAgents) {
        this.monitoringAgents = monitoringAgents;
    }

    public Map<String, AgentJobStatus> getAgentsJobStatus() {
        return agentsJobStatus;
    }

    public Job setAgentsJobStatus(Map<String, AgentJobStatus> agentsJobStatus) {
        this.agentsJobStatus = agentsJobStatus;
        return this;
    }

    // Extra functions : not part of the bean but manipulates the state of bean
    public Job addMonitoringAgent(String agentIp) {
        monitoringAgents.add(agentIp);
        return this;
    }

    public Job jobRunningInAgent(String agentIp) throws IOException {
        this.jobStatus = JOB_STATUS.RUNNING;
        this.agentsJobStatus.put(agentIp, new AgentJobStatus().setAgentIp(agentIp).setJob_status(JOB_STATUS.RUNNING));
        this.persist();
        return this;
    }

    public Job jobCompletedInAgent(String agentIp) throws IOException, ExecutionException, InterruptedException {
        JOB_STATUS jobStatusInAgent = this.agentsJobStatus.get(agentIp).getJob_status();
        if(jobStatusInAgent != JOB_STATUS.KILLED) {
            this.agentsJobStatus.get(agentIp).setJob_status(JOB_STATUS.COMPLETED);
        }

        if(!this.agentsJobStatus.containsValue(JOB_STATUS.RUNNING) &&
                !this.agentsJobStatus.containsValue(JOB_STATUS.PAUSED)) {
            this.jobStatus = JOB_STATUS.COMPLETED;
        }

        AgentsCache.getAgentInfo(agentIp).setFree();
        AgentsCache.getAgentInfo(agentIp).getRunningJobs().remove(jobId);

        if(isCompleted()) {
            this.ended();
        }
        this.persist();
        return this;
    }

    public Job jobKilledInAgent(String agentIp) throws IOException, ExecutionException, InterruptedException {
        JOB_STATUS jobStatusInAgent = this.agentsJobStatus.get(agentIp).getJob_status();
        if(jobStatusInAgent != JOB_STATUS.COMPLETED) {
            this.agentsJobStatus.get(agentIp).setJob_status(JOB_STATUS.KILLED);
        }

        if(!this.agentsJobStatus.containsValue(JOB_STATUS.RUNNING) &&
                !this.agentsJobStatus.containsValue(JOB_STATUS.PAUSED)) {
            this.jobStatus = JOB_STATUS.COMPLETED;
        }

        AgentsCache.getAgentInfo(agentIp).setFree();
        AgentsCache.getAgentInfo(agentIp).getRunningJobs().remove(jobId);

        if(isCompleted()) {
            this.ended();
        }

        this.persist();
        return this;
    }

    /**
     * Mark that job has started
     * @throws IOException
     */
    public void started() throws IOException {
        this.jobStatus = JOB_STATUS.RUNNING;
        this.startTime = new Date();
        this.persist();
    }

    /**
     * Mark that job has ended
     * @throws IOException
     */
    private void ended() throws IOException, ExecutionException, InterruptedException {
        this.endTime = new Date();
        this.stopMonitoring();
        CounterCompoundThread.getCounterCruncherThread().removeJob(jobId);
        CounterThroughputThread.getCounterCruncherThread().removeJob(jobId);
        TimerComputationThread.getComputationThread().removeJob(jobId);

        this.persist();
    }

    /**
     * Mark that job has failed
     * @throws IOException
     */
    public void failedToStart() throws IOException {
        this.jobStatus = JOB_STATUS.FAILED_TO_START;
        this.endTime = new Date();
        this.persist();
    }

    public void start(List<LoaderAgent> freeAgents) {
        String runFile = configuration.getJobFSConfig().getRunFile(this.runName);
        List<LoaderAgent> agentsToUse = new ArrayList<LoaderAgent>();
        for(LoaderAgent freeAgent : freeAgents) {
            agentsToUse.add(freeAgent);
        }

        try {
            PerformanceRun performanceRun = objectMapper.readValue(new File(runFile) , PerformanceRun.class);

            // Persisting Job Json in Local File system.
            this.persistRunInfo(performanceRun);

            // Raising request to monitoring agents to start collecting metrics from on demand resource collectors
            raiseOnDemandResourceRequest(performanceRun.getOnDemandMetricCollections());

            // Raising request to monitoring agents to start publishing collected metrics to Loader server
            raiseMetricPublishRequest(performanceRun.getMetricCollections());

            // Deploy Libraries on Agents
            deployLibrariesOnAgents(performanceRun.getLoadParts(), agentsToUse);

            // Submitting Jobs to Loader Agent
            submitJobToAgents(performanceRun.getLoadParts(), agentsToUse);

            CounterCompoundThread.getCounterCruncherThread().addJob(jobId);
            CounterThroughputThread.getCounterCruncherThread().addJob(jobId);
            TimerComputationThread.getComputationThread().addJob(jobId);

            persist();
        }
        catch (Exception e) {
            log.error("Job Submission Failed",e);
            try {
                this.failedToStart();
                jobCleanUpOnFailure(this);
            } catch (Exception e1) {
                log.error("Job Clean up Failure",e);
            }
        }
    }

    /**
     * Raise On Demand Resource Request to Monitoring Agent as Part of Load Job
     * @param onDemandMetricCollections
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     * @throws IOException
     */
    private void raiseOnDemandResourceRequest(List<OnDemandMetricCollection> onDemandMetricCollections) throws InterruptedException, ExecutionException, IOException {
        for(OnDemandMetricCollection onDemandMetricCollection : onDemandMetricCollections) {
            String agentIp = onDemandMetricCollection.getAgent();
            new MonitoringClient(agentIp,
                    configuration.getMonitoringAgentConfig().getAgentPort()).
                    raiseOnDemandResourceRequest(onDemandMetricCollection.buildRequest(jobId));
            this.addMonitoringAgent(agentIp);
            log.info("Request "+onDemandMetricCollection+" raised on Agent "+agentIp);
        }
    }

    /**
     * Raise Metric Publish request to Monitoring Agent
     * @param metricCollections
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    private void raiseMetricPublishRequest(List<MetricCollection> metricCollections) throws InterruptedException, ExecutionException, IOException {
        for(MetricCollection metricCollection : metricCollections) {
            String agentIp = metricCollection.getAgent();
            new MonitoringClient(agentIp,
                    configuration.getMonitoringAgentConfig().getAgentPort()).
                    raiseMetricPublishRequest(metricCollection.buildRequest(jobId));
            this.addMonitoringAgent(agentIp);
            log.info("Request "+metricCollection+" raised on Agent "+agentIp);
        }
    }

    /**
     * Submitting Load Job To Loader Agents
     *
     * @param loadParts
     * @param agentsToUse
     * @throws IOException
     * @throws perf.server.exception.JobException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void submitJobToAgents(List<LoadPart> loadParts, List<LoaderAgent> agentsToUse)
            throws IOException, JobException, ExecutionException, InterruptedException {
        this.started();
        for(LoadPart loadPart : loadParts) {
            // Submitting Job To Agent
            List<String> classes = loadPart.getClasses();

            StringBuilder classListWithNewLine = new StringBuilder();
            for(String clazz : classes)
                classListWithNewLine.append(clazz+"\n");

            for(int agentI = 1; agentI<=loadPart.getAgents(); agentI++) {
                LoaderAgent agent = agentsToUse.remove(0);
                submitJobToAgent(agent.getIp(),
                        jobId,
                        loadPart.getLoad(),
                        classListWithNewLine.toString());
                this.jobRunningInAgent(agent.getIp());
                AgentsCache.getAgentInfo(agent.getIp()).addRunningJob(jobId);
                AgentsCache.getAgentInfo(agent.getIp()).setBusy();
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
                configuration.getAgentConfig().getAgentPort()).
                submitJob(jobId, load, classListStr);
        log.info("Load Job " + load + " submitted to Agent " + agentIp);
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
     * Kill job in specified agents,
     * @param agents
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws JobException
     * @throws IOException
     */
    public void killJobInAgents(Collection<String> agents)
            throws InterruptedException, ExecutionException, JobException, IOException {
        if(!this.jobStatus.equals(Job.JOB_STATUS.COMPLETED) &&
                !this.jobStatus.equals(Job.JOB_STATUS.KILLED)) {

            Map<String, Job.AgentJobStatus> agentsJobStatusMap = this.getAgentsJobStatus();
            for(String agent : agents) {
                if(!agentsJobStatusMap.get(agent).getJob_status().equals(Job.JOB_STATUS.KILLED) &&
                        !agentsJobStatusMap.get(agent).getJob_status().equals(Job.JOB_STATUS.COMPLETED)) {
                    new LoaderAgentClient(agent, configuration.getAgentConfig().getAgentPort()).killJob(this.jobId);
                    this.jobKilledInAgent(agent);
                }
            }
        }
    }

    /**
     * Stop monitoring in all agents for the job
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void stopMonitoring() throws IOException, ExecutionException, InterruptedException {
        Set<String> monitoringAgentIps = this.getMonitoringAgents();
        for(String agentIp : monitoringAgentIps) {
            MonitoringClient monitoringAgent = new MonitoringClient(agentIp, configuration.getMonitoringAgentConfig().getAgentPort());
            monitoringAgent.deleteOnDemandResourceRequest(jobId);
            monitoringAgent.deletePublishResourceRequest(jobId);
        }
    }

    /**
     * Check for completion
     * @return
     */
    @JsonIgnore
    public boolean isCompleted() {
        return this.getJobStatus().equals(Job.JOB_STATUS.COMPLETED) ||
                        this.getJobStatus().equals(Job.JOB_STATUS.KILLED);
    }

    /**
     * Persist Job Status in File System
     * @throws java.io.IOException
     */
    private void persist() throws IOException {
        objectMapper.writeValue(new FileOutputStream(configuration.getJobFSConfig().getJobStatusFile(jobId)), this);
    }

    /**
     * Persist Run Details for this job in FS
     * @param performanceRun
     * @throws java.io.IOException
     */
    private void persistRunInfo(PerformanceRun performanceRun) throws IOException {
        String runName = performanceRun.getRunName();

        // Add file containing run name in job folder
        String jobRunNameFile = configuration.getJobFSConfig().getJobRunNameFile(jobId);
        FileHelper.createFilePath(jobRunNameFile);
        FileHelper.persistStream(new ByteArrayInputStream(runName.getBytes()),
                jobRunNameFile,
                false);

        // Adding job ids in run folder file
        String runJobsFile = configuration.getJobFSConfig().getRunJobsFile(runName);
        FileHelper.createFilePath(runJobsFile);
        FileHelper.persistStream(new ByteArrayInputStream((jobId + "\n").getBytes()),
                runJobsFile,
                true);
    }

    /**
     * Clean up Running Job In case Something went wrong while job was being submitted
     * @param job
     */
    private static void jobCleanUpOnFailure(Job job) throws InterruptedException, ExecutionException, JobException, IOException {
        job.killJobInAgents(job.getAgentsJobStatus().keySet());
        job.stopMonitoring();
    }

    /**
     * Search Jobs in File System
     * @param searchJobId
     * @param searchRunName
     * @param searchJobStatusList
     * @return
     */
    public static List<Job> searchJobs(String searchJobId, String searchRunName, List<String> searchJobStatusList) throws IOException {
        List<Job> jobs = new ArrayList<Job>();
        File runsPath = new File(configuration.getJobFSConfig().getRunsPath());
        for(File runPath : runsPath.listFiles()) {
            if(runPath.getName().toUpperCase().contains(searchRunName.toUpperCase())) {
                File runJobsFile = new File(configuration.getJobFSConfig().getRunJobsFile(runPath.getName()));
                if(runJobsFile.exists()) {
                    BufferedReader runJobsFileReader = FileHelper.bufferedReader(runJobsFile.getAbsolutePath());
                    String runJobId;
                    while((runJobId = runJobsFileReader.readLine()) != null) {
                        if(runJobId.toUpperCase().contains(searchJobId.toUpperCase())) {
                            Job job = objectMapper.readValue(new File(configuration.getJobFSConfig().getJobStatusFile(runJobId)), Job.class);
                            if(searchJobStatusList.contains("ALL")) {
                                jobs.add(job);
                            }
                            else if(searchJobStatusList.contains(job.getJobStatus().toString())) {
                                jobs.add(job);
                            }
                        }
                    }
                }
            }
        }
        return jobs;
    }

}
