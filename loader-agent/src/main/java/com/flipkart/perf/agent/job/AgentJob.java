package com.flipkart.perf.agent.job;

import com.flipkart.perf.agent.client.JobClient;
import com.flipkart.perf.common.jackson.ObjectMapperUtil;
import com.flipkart.perf.common.util.FileHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.flipkart.perf.agent.client.LoaderServerClient;
import com.flipkart.perf.agent.config.JobFSConfig;
import com.flipkart.perf.agent.config.LoaderAgentConfiguration;
import com.flipkart.perf.agent.daemon.JobHealthCheckThread;
import com.flipkart.perf.agent.daemon.JobStatsSyncThread;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * nitinka
 * Information about the job that is being executed.
 */

public class AgentJob {
    public static enum JOB_STATUS {
        QUEUED, RUNNING, COMPLETED, KILLED, ERROR;
    }

    private static Logger log = LoggerFactory.getLogger(AgentJob.class);
    private static final JobFSConfig jobFSConfig = LoaderAgentConfiguration.instance().getJobFSConfig();
    private static final ObjectMapper objectMapper = ObjectMapperUtil.instance();
    private JOB_STATUS jobStatus = JOB_STATUS.QUEUED;

    private String jobId, jobCmd;
    private long startTime, endTime;
    private int jmxPort;
    private int httpPort;

    public String getJobId() {
        return jobId;
    }

    public AgentJob setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    public String getJobCmd() {
        return jobCmd;
    }

    public AgentJob setJobCmd(String jobCmd) {
        this.jobCmd = jobCmd;
        return this;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    public AgentJob setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
        return this;
    }

    public AgentJob setHttpPort(int httpPort) {
        this.httpPort = httpPort;
        return this;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public JOB_STATUS getJobStatus() {
        return jobStatus;
    }

    public AgentJob setJobStatus(JOB_STATUS jobStatus) {
        this.jobStatus = jobStatus;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public AgentJob setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getEndTime() {
        return endTime;
    }

    public AgentJob setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    public void queued() throws IOException {
        this.jobStatus = JOB_STATUS.QUEUED;
        persist();
    }

    public void started() throws IOException {
        this.startTime = System.currentTimeMillis();
        this.jobStatus = JOB_STATUS.RUNNING;

        List<String> runningJobs = objectMapper.readValue(new File(jobFSConfig.getRunningJobsFile()), List.class);
        runningJobs.add(jobId);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(jobFSConfig.getRunningJobsFile()), runningJobs);

        JobStatsSyncThread.instance().addJob(jobId);
        JobHealthCheckThread.instance().addJob(this);
        persist();
    }

    public AgentJob kill() throws IOException, InterruptedException, ExecutionException {
        JobClient.killJob(this.jobId);
        killed();
        return this;
    }

    public void killed() throws IOException, ExecutionException, InterruptedException {
        completed(JOB_STATUS.KILLED);
    }

    public void completed() throws IOException, ExecutionException, InterruptedException {
        completed(JOB_STATUS.COMPLETED);
    }

    public void errored() throws IOException, ExecutionException, InterruptedException {
        completed(JOB_STATUS.ERROR);
    }

    private void completed(JOB_STATUS status) throws IOException, ExecutionException, InterruptedException {
        this.jobStatus = status;
        this.endTime = System.currentTimeMillis();
        JobStatsSyncThread.instance().removeJob(jobId);
        JobHealthCheckThread.instance().removeJob(this);

        List<String> runningJobs = objectMapper.readValue(new File(jobFSConfig.getRunningJobsFile()), List.class);
        runningJobs.remove(jobId);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(jobFSConfig.getRunningJobsFile()), runningJobs);

        persist();
        LoaderServerClient.
                buildClient(LoaderAgentConfiguration.instance().getServerInfo()).
                notifyJobIsOver(this.jobId, status.toString());
    }

    private void persist() throws IOException {
        String jobFile = jobFSConfig.getJobFile(jobId);
        FileHelper.createFilePath(jobFile);
        ObjectMapperUtil.instance().
                writerWithDefaultPrettyPrinter().
                writeValue(new File(jobFile), this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentJob)) return false;

        AgentJob agentJob = (AgentJob) o;

        if (jmxPort != agentJob.jmxPort) return false;
        if (jobCmd != null ? !jobCmd.equals(agentJob.jobCmd) : agentJob.jobCmd != null) return false;
        if (jobId != null ? !jobId.equals(agentJob.jobId) : agentJob.jobId != null) return false;

        return true;
    }
}
