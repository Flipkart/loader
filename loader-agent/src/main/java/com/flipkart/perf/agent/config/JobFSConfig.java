package com.flipkart.perf.agent.config;

public class JobFSConfig {
    private String jobBasePath, jobPath, jobLogFile, jobFile, runningJobsFile;

    public String getJobBasePath() {
        return jobBasePath;
    }

    public JobFSConfig setJobBasePath(String jobBasePath) {
        this.jobBasePath = jobBasePath;
        return this;
    }

    public String getJobPath(String jobId) {
        return jobPath.replace("{jobId}", jobId);
    }

    public void setJobPath(String jobPath) {
        this.jobPath = jobPath;
    }

    public String getJobLogFile(String jobId) {
        return jobLogFile.replace("{jobId}", jobId);
    }

    public void setJobLogFile(String jobLogFile) {
        this.jobLogFile = jobLogFile;
    }

    public String getJobFile(String jobId) {
        return jobFile.replace("{jobId}", jobId);
    }

    public void setJobFile(String jobFile) {
        this.jobFile = jobFile;
    }

    public String getRunningJobsFile() {
        return runningJobsFile;
    }

    public void setRunningJobsFile(String runningJobsFile) {
        this.runningJobsFile = runningJobsFile;
    }
}
