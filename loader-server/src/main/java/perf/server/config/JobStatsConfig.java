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
    private String jobFile;
    private String runFile;

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

    public String getJobFile() {
        return jobFile;
    }

    public void setJobFile(String jobFile) {
        this.jobFile = jobFile;
    }

    public String getRunFile() {
        return runFile;
    }

    public void setRunFile(String runFile) {
        this.runFile = runFile;
    }
}
