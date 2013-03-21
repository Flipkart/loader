package perf.server.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 2/2/13
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobFSConfig {
    private String combinedStatsPath;
    private String jobAgentStatsPath;
    private String jobResourceMonitoringFile;
    private String runFile;
    private String jobRunNameFile;
    private String runsPath;
    private String jobsPath;
    private String jobPath;

    public String getJobStatsPath(String jobId) {
        return combinedStatsPath.replace("{jobId}", jobId);
    }

    public void setCombinedStatsPath(String combinedStatsPath) {
        this.combinedStatsPath = combinedStatsPath;
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

    public String getJobAgentStatsPath(String jobId, String agentIp) {
        return jobAgentStatsPath.replace("{jobId}", jobId).
                replace("{agentIp}", agentIp);
    }

    public void setJobAgentStatsPath(String jobAgentStatsPath) {
        this.jobAgentStatsPath = jobAgentStatsPath;
    }

    public String getJobPath(String jobId) {
        return jobPath.replace("{jobId}", jobId);
    }

    public void setJobPath(String jobPath) {
        this.jobPath = jobPath;
    }
}


