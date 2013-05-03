package perf.agent.job;

import com.open.perf.jackson.ObjectMapperUtil;
import com.open.perf.util.FileHelper;
import org.codehaus.jackson.map.ObjectMapper;
import perf.agent.config.JobFSConfig;
import perf.agent.config.LoaderAgentConfiguration;
import perf.agent.daemon.JobHealthCheckThread;
import perf.agent.daemon.JobStatsSyncThread;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * nitinka
 * Information about the job that is being executed.
 */

public class Job {
    public static enum JOB_STATUS {
        QUEUED, RUNNING, COMPLETED, KILLED, ERROR;
    }
    private static final JobFSConfig jobFSConfig = LoaderAgentConfiguration.instance().getJobFSConfig();
    private static final ObjectMapper objectMapper = ObjectMapperUtil.instance();
    private String jobId, jobCmd;
    private JOB_STATUS jobStatus = JOB_STATUS.QUEUED;
    private long startTime, endTime;
    private int jmxPort;

    public String getJobId() {
        return jobId;
    }

    public Job setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    public String getJobCmd() {
        return jobCmd;
    }

    public Job setJobCmd(String jobCmd) {
        this.jobCmd = jobCmd;
        return this;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    public Job setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
        return this;
    }

    public JOB_STATUS getJobStatus() {
        return jobStatus;
    }

    public Job setJobStatus(JOB_STATUS jobStatus) {
        this.jobStatus = jobStatus;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public Job setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getEndTime() {
        return endTime;
    }

    public Job setEndTime(long endTime) {
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

    public void kill() throws IOException, InterruptedException {
        Runtime.getRuntime().exec(new String[]{"/bin/sh","-c","kill -9 `ps aux | grep "+jobId+" | grep -v grep | tr -s \" \" \":\" |cut -f 2 -d \":\"`"}).
                waitFor();
        killed();
    }

    public void killed() throws IOException {
        completed(JOB_STATUS.KILLED);
    }

    public void completed() throws IOException {
        completed(JOB_STATUS.COMPLETED);
    }

    public void errored() throws IOException {
        completed(JOB_STATUS.ERROR);
    }

    private void completed(JOB_STATUS status) throws IOException {
        this.jobStatus = status;
        this.endTime = System.currentTimeMillis();
        JobStatsSyncThread.instance().removeJob(jobId);
        JobHealthCheckThread.instance().removeJob(this);

        List<String> runningJobs = objectMapper.readValue(new File(jobFSConfig.getRunningJobsFile()), List.class);
        runningJobs.remove(jobId);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(jobFSConfig.getRunningJobsFile()), runningJobs);

        persist();
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
        if (!(o instanceof Job)) return false;

        Job job = (Job) o;

        if (jmxPort != job.jmxPort) return false;
        if (jobCmd != null ? !jobCmd.equals(job.jobCmd) : job.jobCmd != null) return false;
        if (jobId != null ? !jobId.equals(job.jobId) : job.jobId != null) return false;

        return true;
    }
}
