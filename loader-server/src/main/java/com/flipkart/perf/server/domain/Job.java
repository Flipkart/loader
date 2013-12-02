package com.flipkart.perf.server.domain;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.domain.Load;
import com.flipkart.perf.server.util.ResponseBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.perf.server.cache.AgentsCache;
import com.flipkart.perf.server.client.LoaderAgentClient;
import com.flipkart.perf.server.client.MonitoringClient;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.daemon.*;
import com.flipkart.perf.server.exception.JobException;
import com.flipkart.perf.server.exception.LibNotDeployedException;
import com.flipkart.perf.server.util.DeploymentHelper;
import com.flipkart.perf.server.cache.JobsCache;
import com.flipkart.perf.server.util.ObjectMapperUtil;

import javax.ws.rs.WebApplicationException;

public  class Job {

    private static final LoaderServerConfiguration configuration = LoaderServerConfiguration.instance();
    private static final ObjectMapper objectMapper = ObjectMapperUtil.instance();
    private static Logger logger = LoggerFactory.getLogger(Job.class);

   // public abstract void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException;

    public static enum JOB_STATUS {
        QUEUED, RUNNING, PAUSED, COMPLETED, KILLED, FAILED_TO_START, ERROR;
    }

    private String jobId;
    private String runName;
    private Date startTime, endTime;
    private JOB_STATUS jobStatus;
    private String failedToStartReason = ""; // Will be set only in case when job failed to start
    private Map<String,AgentJobStatus> agentsJobStatus = new HashMap<String, AgentJobStatus>();
    private Set<String> monitoringAgents;
    private String remarks = "";
    private String logLevel = "INFO";

    public static Job raiseJobRequest(JobRequest jobRequest) throws IOException {
        //runExistsOrException(jobRequest.getRunName());
        Job job = new Job().
                setJobId(UUID.randomUUID().toString()).
                setRunName(jobRequest.getRunName());

        job.persistRunInfo();
        JobDispatcherThread.instance().addJobRequest(job);
        return job;
    }

    public static class AgentJobStatus {
        private String agentIp;
        private Boolean inStress;
        private JOB_STATUS job_status;
        private Map<String, Object> healthStatus = new HashMap<String,Object>();

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
                if(inStress == null) {
                    Object inStressObject = this.healthStatus.remove("inStress");
                    this.inStress = inStressObject == null ? false : Boolean.parseBoolean(inStressObject.toString());
                }
            }
            return this;
        }

        public boolean isInStress() {
            return inStress == null ? false : inStress.booleanValue();
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
        if(jobStatusInAgent != JOB_STATUS.KILLED && jobStatusInAgent != JOB_STATUS.ERROR) {
            this.agentsJobStatus.get(agentIp).setJob_status(JOB_STATUS.COMPLETED);
        }

        return jobOverInAgent(agentIp);
    }

    public Job jobKilledInAgent(String agentIp) throws IOException, ExecutionException, InterruptedException {
        JOB_STATUS jobStatusInAgent = this.agentsJobStatus.get(agentIp).getJob_status();
        if(jobStatusInAgent != JOB_STATUS.COMPLETED && jobStatusInAgent != JOB_STATUS.ERROR) {
            this.agentsJobStatus.get(agentIp).setJob_status(JOB_STATUS.KILLED);
        }

        return jobOverInAgent(agentIp);
    }

    public Job jobErrorInAgent(String agentIp) throws InterruptedException, ExecutionException, IOException {
        JOB_STATUS jobStatusInAgent = this.agentsJobStatus.get(agentIp).getJob_status();
        if(jobStatusInAgent != JOB_STATUS.KILLED && jobStatusInAgent != JOB_STATUS.COMPLETED) {
            this.agentsJobStatus.get(agentIp).setJob_status(JOB_STATUS.ERROR);
        }

        return jobOverInAgent(agentIp);
    }

    private Job jobOverInAgent(String agentIp) throws IOException, ExecutionException, InterruptedException {
        if(!this.agentsJobStatus.containsValue(JOB_STATUS.RUNNING) &&
                !this.agentsJobStatus.containsValue(JOB_STATUS.PAUSED)) {
            this.jobStatus = JOB_STATUS.COMPLETED;
        }

        AgentsCache.getAgentInfo(agentIp).setFree().removeJob(jobId).persist();

        if(isCompleted()) {
            this.ended();
        }

        this.persist();
        return this;
    }

    /**
     * Mark that job has been queued
     */
    public void queued() throws IOException {
        this.jobStatus = JOB_STATUS.QUEUED;

        // Adding to Queued Jobs File
        List<String> queuedJobs = objectMapper.readValue(new File(configuration.getJobFSConfig().getQueuedJobsFile()), List.class);
        queuedJobs.add(jobId);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(configuration.getJobFSConfig().getQueuedJobsFile()), queuedJobs);

        this.persist();
        JobsCache.put(jobId, this);
    }


    /**
     * Mark that job has started
     * @throws IOException
     */
    public void started() throws IOException {
        this.jobStatus = JOB_STATUS.RUNNING;

        // Add to Running Jobs file
        List<String> runningJobs = objectMapper.readValue(new File(configuration.getJobFSConfig().getRunningJobsFile()), List.class);
        runningJobs.add(jobId);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(configuration.getJobFSConfig().getRunningJobsFile()), runningJobs);

        // Remove from Queued Jobs File
        List<String> queuedJobs = objectMapper.readValue(new File(configuration.getJobFSConfig().getQueuedJobsFile()), List.class);
        queuedJobs.remove(jobId);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(configuration.getJobFSConfig().getQueuedJobsFile()), queuedJobs);

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
        CounterCompoundThread.instance().removeJob(jobId);
//        CounterThroughputThread.instance().removeJob(jobId);
        TimerComputationThread.instance().removeJob(jobId);
        HistogramComputationThread.instance().removeJob(jobId);
        GroupConfConsolidationThread.instance().removeJob(jobId);

        // Remove from Running Jobs File
        List<String> runningJobs = objectMapper.readValue(new File(configuration.getJobFSConfig().getRunningJobsFile()), List.class);
        runningJobs.remove(jobId);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(configuration.getJobFSConfig().getRunningJobsFile()), runningJobs);

        this.persist();
    }

    public void killed() throws InterruptedException, ExecutionException, IOException {
        this.jobStatus = JOB_STATUS.KILLED;
        ended();
    }



    /**
     * Mark that job has failed
     * @throws IOException
     */
    public void failedToStart(String reason) throws IOException {
        this.jobStatus = JOB_STATUS.FAILED_TO_START;
        this.failedToStartReason = reason;
        this.endTime = new Date();
        this.persist();
    }

    public void start(List<LoaderAgent> freeAgents) {
        List<LoaderAgent> agentsToUse = new ArrayList<LoaderAgent>();
        for(LoaderAgent freeAgent : freeAgents) {
            agentsToUse.add(freeAgent);
        }

        try {
            String runFile = configuration.getJobFSConfig().getRunFile(this.runName);
            PerformanceRun performanceRun = this.performanceRun();

            // Raising request to monitoring agents to start collecting metrics from on demand resource collectors
            raiseOnDemandResourceRequest(performanceRun.getOnDemandMetricCollections());

            // Raising request to monitoring agents to start publishing collected metrics to Loader server
            raiseMetricPublishRequest(performanceRun.getMetricCollections());

            // Deploy Libraries on Agents
            deployLibrariesOnAgents(performanceRun.getLoadParts(), agentsToUse);

            // Deploy Input File Resource On Agents
            deployInputFileResourcesOnAgents(performanceRun.getLoadParts(), agentsToUse);

            // Submitting Jobs to Loader Agent
            submitJobToAgents(performanceRun.getLoadParts(), agentsToUse);

            CounterCompoundThread.instance().addJob(jobId);
//            CounterThroughputThread.instance().addJob(jobId);
            TimerComputationThread.instance().addJob(jobId);
            HistogramComputationThread.instance().addJob(jobId);
            GroupConfConsolidationThread.instance().addJob(jobId);

            persist();
        }
        catch (Exception e) {
            logger.error("Job Submission Failed",e);
            try {
                this.failedToStart(e.getMessage());
                jobCleanUpOnFailure(this);
            } catch (Exception e1) {
                logger.error("Job Clean up Failure",e);
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
            logger.info("Request "+onDemandMetricCollection+" raised on Agent "+agentIp);
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
            logger.info("Request "+metricCollection+" raised on Agent "+agentIp);
        }
    }

    /**
     * Submitting Load Job To Loader Agents
     *
     * @param loadParts
     * @param agentsToUse
     * @throws IOException
     * @throws com.flipkart.perf.server.exception.JobException
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
        logger.info("Agent Ip :" + agentIp);
        new LoaderAgentClient(agentIp,
                configuration.getAgentConfig().getAgentPort()).
                submitJob(jobId, load, classListStr);
        logger.info("Load Job " + load + " submitted to Agent " + agentIp);
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
            throws IOException, JobException, ExecutionException, InterruptedException, LibNotDeployedException {
        for(LoadPart loadPart : loadParts) {
            List<String> classes = loadPart.getClasses();

            StringBuilder classListWithNewLine = new StringBuilder();
            for(String clazz : classes)
                classListWithNewLine.append(clazz+"\n");

            for(LoaderAgent agent : agentsToUse) {
                DeploymentHelper.instance().deployPlatformLibsOnAgent(agent.getIp());
                DeploymentHelper.instance().deployUDFLibsOnAgent(agent.getIp(), classListWithNewLine.toString().trim());
            }
        }
    }

    /**
     *
     * @param loadParts
     * @param agentsToUse
     */
    private void deployInputFileResourcesOnAgents(List<LoadPart> loadParts, List<LoaderAgent> agentsToUse)
            throws InterruptedException, ExecutionException, LibNotDeployedException, IOException {
        for(LoaderAgent agent : agentsToUse) {
            for(LoadPart loadPart : loadParts) {
                DeploymentHelper.instance().deployInputFilesOnAgent(agent.getIp(), loadPart.getInputFileResources());
            }
        }
    }


    public void kill() throws InterruptedException, ExecutionException, IOException {
        if(this.isQueued()) {
            JobDispatcherThread.instance().removeJobRequest(this);
        }
        else if(this.isRunning()) {
            this.killJobInAgents(this.getAgentsJobStatus().keySet());
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
    public void killJobInAgents(Collection<String> agents) throws InterruptedException, ExecutionException, IOException {
        if(!this.jobStatus.equals(Job.JOB_STATUS.COMPLETED) &&
                !this.jobStatus.equals(Job.JOB_STATUS.KILLED)) {

            Map<String, Job.AgentJobStatus> agentsJobStatusMap = this.getAgentsJobStatus();
            for(String agent : agents) {
                if(!agentsJobStatusMap.get(agent).getJob_status().equals(Job.JOB_STATUS.KILLED) &&
                        !agentsJobStatusMap.get(agent).getJob_status().equals(Job.JOB_STATUS.COMPLETED)) {
                    logger.info("Killing Job '"+jobId+"' in agent '"+agent+"'");
                    try {
                        new LoaderAgentClient(agent, configuration.getAgentConfig().getAgentPort()).killJob(this.jobId);
                        this.jobKilledInAgent(agent);
                    } catch (Exception e) {
                        logger.error("Error while killing job at agent "+agent, e);
                        throw new WebApplicationException(ResponseBuilder.internalServerError(e));
                    }
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
                this.getJobStatus().equals(Job.JOB_STATUS.KILLED) ||
                this.getJobStatus().equals(JOB_STATUS.ERROR)||
                this.getJobStatus().equals(JOB_STATUS.FAILED_TO_START);
    }

    /**
     * Persist Job Status in File System
     * @throws java.io.IOException
     */
    public void persist() throws IOException {
        File jobStatusFile = new File(configuration.getJobFSConfig().getJobStatusFile(jobId));
        if(!jobStatusFile.exists())
            FileHelper.createFilePath(jobStatusFile.getAbsolutePath());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(jobStatusFile), this);
    }

    public void persistRunInfo() throws IOException {
        String runFile = configuration.getJobFSConfig().getRunFile(this.runName);
        PerformanceRun performanceRun = objectMapper.readValue(new File(runFile) , PerformanceRun.class);

        // Add file containing run name in job folder
        String jobRunNameFile = configuration.getJobFSConfig().getJobRunFile(jobId);
        FileHelper.createFilePath(jobRunNameFile);
        objectMapper.defaultPrettyPrintingWriter().writeValue(new File(jobRunNameFile), performanceRun);

        // Adding job ids in run folder file
        String runName = performanceRun.getRunName();
        String runJobsFile = configuration.getJobFSConfig().getRunJobsFile(runName);
        FileHelper.createFilePath(runJobsFile);
        FileHelper.persistStream(new ByteArrayInputStream((jobId + "\n").getBytes()),
                runJobsFile,
                true);
    }

    @JsonIgnore
    public PerformanceRun performanceRun() {
        try {
            if(new File(configuration.getJobFSConfig().getJobRunFile(jobId)).exists()) {
                return ObjectMapperUtil.instance().
                        readValue(new File(configuration.getJobFSConfig().getJobRunFile(jobId)), PerformanceRun.class);
            }
            return PerformanceRun.runExistsOrException(this.runName);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
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
    public static List<Job> searchJobs(String searchJobId, String searchRunName, List<String> searchJobStatusList) throws IOException, ExecutionException {
        List<Job> jobs = new ArrayList<Job>();
        File runsPath = new File(configuration.getJobFSConfig().getRunsPath());
        for(File runPath : runsPath.listFiles()) {
            if(runPath.getName().toUpperCase().contains(searchRunName.toUpperCase())) {
                File runJobsFile = new File(configuration.getJobFSConfig().getRunJobsFile(runPath.getName()));
                if(runJobsFile.exists()) {
                    BufferedReader runJobsFileReader = FileHelper.bufferedReader(runJobsFile.getAbsolutePath());
                    String runJobId;
                    while((runJobId = runJobsFileReader.readLine()) != null) {
                        if(runJobId.trim().equals("")) {
                            logger.warn("Empty Job Id Found for run '"+runPath.getName()+"'");
                            continue;
                        }
                        if(runJobId.toUpperCase().contains(searchJobId.toUpperCase())) {
                            Job job = JobsCache.getJob(runJobId);
                            //Job job = objectMapper.readValue(new File(configuration.getJobFSConfig().getJobStatusFile(runJobId)), Job.class);

                            if(job != null) {
                                // In some corner cases Job status file is not persisted and hence job found here would be null.
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
        }
        return jobs;
    }

    public List<AgentJobStatus> aliveAgents() {
        List<AgentJobStatus> aliveAgents = new ArrayList<AgentJobStatus>();
        for(AgentJobStatus agent : this.agentsJobStatus.values()) {
            if(agent.getJob_status() == JOB_STATUS.RUNNING)
                aliveAgents.add(agent);
        }
        return aliveAgents;
    }

    @JsonIgnore
    public boolean isRunning() {
        return jobStatus == JOB_STATUS.RUNNING;
    }

    @JsonIgnore
    public boolean isQueued() {
        return jobStatus == JOB_STATUS.QUEUED;
    }

    public String getRemarks() {
        return remarks;
    }

    public Job setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public String getFailedToStartReason() {
        return failedToStartReason;
    }

    public void setFailedToStartReason(String failedToStartReason) {
        this.failedToStartReason = failedToStartReason;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public void delete() {
        if(this.isCompleted()) {
            FileHelper.remove(configuration.getJobFSConfig().getJobPath(this.jobId));
            JobsCache.removeJob(this.jobId);
        }
        else {
            throw new WebApplicationException(ResponseBuilder.badRequest("Job id "+this.jobId+" not completed yet"));
        }
    }
}
