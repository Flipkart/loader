package perf.agent.job;

/**
 * nitinka
 * Information about the job that is being executed.
 */
public class JobInfo {
    private String jobId, jobCmd;
    private int jmxPort;

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

    public int getJmxPort() {
        return jmxPort;
    }

    public JobInfo setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobInfo)) return false;

        JobInfo jobInfo = (JobInfo) o;

        if (jmxPort != jobInfo.jmxPort) return false;
        if (jobCmd != null ? !jobCmd.equals(jobInfo.jobCmd) : jobInfo.jobCmd != null) return false;
        if (jobId != null ? !jobId.equals(jobInfo.jobId) : jobInfo.jobId != null) return false;

        return true;
    }
}
