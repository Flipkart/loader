package perf.server.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 2/2/13
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobFSConfig {
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
    private String jobMetricStatsFile;
    private String jobMonitoringStatsPath;
    private String jobResourceMonitoringFile;
    private String jobHealthStatusFile;

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



    public String getJobFunctionStatsFile(String jobId, String groupName, String metricType, String metricName, String agentIp) {
        return jobMetricStatsFile.
                replace("{jobId}", jobId).
                replace("{groupName}", groupName).
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
}



