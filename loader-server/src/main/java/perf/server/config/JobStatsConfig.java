package perf.server.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 2/2/13
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobStatsConfig {
    private String jobStatsFolder;
    private String jobMonitoringStats;
    private String runJobMappingFile;
    private String runFile;
    private String jobRunNameFile;

    public String getJobStatsFolder() {
        return jobStatsFolder;
    }

    public void setJobStatsFolder(String jobStatsFolder) {
        this.jobStatsFolder = jobStatsFolder;
    }

    public String getJobMonitoringStats() {
        return jobMonitoringStats;
    }

    public void setJobMonitoringStats(String jobMonitoringStats) {
        this.jobMonitoringStats = jobMonitoringStats;
    }

    public String getRunJobMappingFile() {
        return runJobMappingFile;
    }

    public void setRunJobMappingFile(String runJobMappingFile) {
        this.runJobMappingFile = runJobMappingFile;
    }

    public String getRunFile() {
        return runFile;
    }

    public void setRunFile(String runFile) {
        this.runFile = runFile;
    }

    public String getJobRunNameFile() {
        return jobRunNameFile;
    }

    public void setJobRunNameFile(String jobRunNameFile) {
        this.jobRunNameFile = jobRunNameFile;
    }
}
