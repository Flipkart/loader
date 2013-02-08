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
}
