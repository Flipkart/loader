package com.flipkart.perf.server.config;

/**
 * Manages Performance Job related Files
 */
public class JobFSConfig {

    private String businessUnitsPath;
    private String businessUnitFile;
    private String runsPath;
    private String runPath;
    private String runFile;
    private String jobRunNameFile;
    private String jobsPath;
    private String jobPath;
    private String jobStatusFile;
    private String runJobsFile;
    private String jobStatsPath;
    private String jobGroupStatsPath;
    private String jobFunctionStatsPath;
    private String jobFunctionTimersStatsPath;
    private String jobFunctionCountersStatsPath;
    private String jobFunctionHistogramStatsPath;
    private String jobMetricStatsFile;
    private String jobMonitoringStatsPath;
    private String jobResourceMonitoringFile;
    private String jobHealthStatusFile;
    private String runningJobsFile;
    private String queuedJobsFile;
    private String jobGroupConfFile;

    public String getBusinessUnitsPath() {
        return businessUnitsPath;
    }

    public JobFSConfig setBusinessUnitsPath(String businessUnitsPath) {
        this.businessUnitsPath = businessUnitsPath;
        return this;
    }

    public String getBusinessUnitFile(String businessUnit) {
        return businessUnitFile.replace("{businessUnit}", businessUnit);
    }

    public JobFSConfig setBusinessUnitFile(String businessUnitFile) {
        this.businessUnitFile= businessUnitFile;
        return this;
    }

    public String getJobResourceMonitoringFile(String jobId, String agentIp, String resource) {
        return jobResourceMonitoringFile.replace("{jobId}", jobId).
                replace("{agentIp}", agentIp).
                replace("{resource}", resource);
    }

    public void setJobResourceMonitoringFile(String jobResourceMonitoringFile) {
        this.jobResourceMonitoringFile = jobResourceMonitoringFile;
    }

    public String getRunFile(String runName) {
        return runFile.replace("{runName}", runName);
    }

    public String getJobMonitoringStatsPath(String jobId) {
        return jobMonitoringStatsPath.replace("{jobId}", jobId);
    }

    public void setJobMonitoringStatsPath(String jobMonitoringStatsPath) {
        this.jobMonitoringStatsPath = jobMonitoringStatsPath;
    }

    public void setRunFile(String runFile) {
        this.runFile = runFile;
    }

    public String getJobRunNameFile(String jobId) {
        return jobRunNameFile.replace("{jobId}", jobId);
    }

    public void setJobRunNameFile(String jobRunNameFile) {
        this.jobRunNameFile = jobRunNameFile;
    }

    public String getRunsPath() {
        return runsPath;
    }

    public void setRunsPath(String runsPath) {
        this.runsPath = runsPath;
    }

    public String getJobsPath() {
        return jobsPath;
    }

    public void setJobsPath(String jobsPath) {
        this.jobsPath = jobsPath;
    }

    public String getJobPath(String jobId) {
        return jobPath.replace("{jobId}", jobId);
    }

    public void setJobPath(String jobPath) {
        this.jobPath = jobPath;
    }

    public String getJobStatusFile(String jobId) {
        return jobStatusFile.replace("{jobId}", jobId);
    }

    public void setJobStatusFile(String jobStatusFile) {
        this.jobStatusFile = jobStatusFile;
    }

    public String getRunPath(String runName) {
        return runPath.replace("{runName}", runName);
    }

    public void setRunPath(String runPath) {
        this.runPath = runPath;
    }

    public String getRunJobsFile(String runName) {
        return runJobsFile.replace("{runName}", runName);
    }

    public void setRunJobsFile(String runJobsFile) {
        this.runJobsFile = runJobsFile;
    }

    public String getJobGroupStatsPath(String jobId, String groupName) {
        return jobGroupStatsPath.
                replace("{jobId}", jobId).
                replace("{groupName}", groupName);
    }

    public void setJobGroupStatsPath(String jobGroupStatsPath) {
        this.jobGroupStatsPath = jobGroupStatsPath;
    }

    public String getJobStatsPath(String jobId) {
        return jobStatsPath.replace("{jobId}", jobId);
    }

    public void setJobStatsPath(String jobStatsPath) {
        this.jobStatsPath = jobStatsPath;
    }

    public String getJobFunctionStatsFile(String jobId, String groupName, String functionName, String metricType, String metricName, String agentIp) {
        return jobMetricStatsFile.
                replace("{jobId}", jobId).
                replace("{groupName}", groupName).
                replace("{functionName}", functionName).
                replace("{metricType}", metricType).
                replace("{metricName}", metricName).
                replace("{agentIp}", agentIp);
    }

    public void setJobMetricStatsFile(String jobMetricStatsFile) {
        this.jobMetricStatsFile = jobMetricStatsFile;
    }

    public String getJobHealthStatusFile(String jobId, String agentIp) {
        return jobHealthStatusFile.
                replace("{jobId}", jobId).
                replace("{agentIp}", agentIp);
    }

    public void setJobHealthStatusFile(String jobHealthStatusFile) {
        this.jobHealthStatusFile = jobHealthStatusFile;
    }

    public String getRunningJobsFile() {
        return runningJobsFile;
    }

    public void setRunningJobsFile(String runningJobsFile) {
        this.runningJobsFile = runningJobsFile;
    }

    public String getQueuedJobsFile() {
        return queuedJobsFile;
    }

    public void setQueuedJobsFile(String queuedJobsFile) {
        this.queuedJobsFile = queuedJobsFile;
    }

    public void setJobGroupConfFile(String jobGroupConfFile) {
        this.jobGroupConfFile = jobGroupConfFile;
    }

    public String getJobGroupConfFile(String jobId, String groupName, String agent) {
        return jobGroupConfFile.
                replace("{jobId}", jobId).
                replace("{agentIp}",agent).
                replace("{groupName}", groupName);
    }

    public String getJobFunctionStatsPath(String jobId, String groupName, String functionName) {
        return jobFunctionStatsPath.
                replace("{jobId}", jobId).
                replace("{groupName}", groupName).
                replace("{functionName}", functionName);
    }

    public void setJobFunctionStatsPath(String jobFunctionStatsPath) {
        this.jobFunctionStatsPath = jobFunctionStatsPath;
    }

    public String getJobFunctionTimersStatsPath(String jobId, String groupName, String functionName) {
        return jobFunctionTimersStatsPath.
                replace("{jobId}", jobId).
                replace("{groupName}", groupName).
                replace("{functionName}", functionName);
    }

    public void setJobFunctionTimersStatsPath(String jobFunctionTimersStatsPath) {
        this.jobFunctionTimersStatsPath = jobFunctionTimersStatsPath;
    }

    public String getJobFunctionCountersStatsPath(String jobId, String groupName, String functionName) {
        return jobFunctionCountersStatsPath.
                replace("{jobId}", jobId).
                replace("{groupName}", groupName).
                replace("{functionName}", functionName);
    }

    public void setJobFunctionCountersStatsPath(String jobFunctionCountersStatsPath) {
        this.jobFunctionCountersStatsPath = jobFunctionCountersStatsPath;
    }

    public String getJobFunctionHistogramStatsPath(String jobId, String groupName, String functionName) {
        return jobFunctionHistogramStatsPath.
                replace("{jobId}", jobId).
                replace("{groupName}", groupName).
                replace("{functionName}", functionName);
    }

    public void setJobFunctionHistogramStatsPath(String jobFunctionHistogramStatsPath) {
        this.jobFunctionHistogramStatsPath = jobFunctionHistogramStatsPath;
    }
}



