package perf.agent.job;

/**
 * nitinka
 * Information about the job that is being executed.
 */
public class JobInfo {
    private String jobId, jobCmd;
    private int port;

    public String getJobId() {
        return jobId;
    }

    public JobInfo setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    public String getJobCmd() {
        return jobCmd;
    }

    public JobInfo setJobCmd(String jobCmd) {
        this.jobCmd = jobCmd;
        return this;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobInfo)) return false;

        JobInfo jobInfo = (JobInfo) o;

        if (port != jobInfo.port) return false;
        if (jobCmd != null ? !jobCmd.equals(jobInfo.jobCmd) : jobInfo.jobCmd != null) return false;
        if (jobId != null ? !jobId.equals(jobInfo.jobId) : jobInfo.jobId != null) return false;

        return true;
    }
}
