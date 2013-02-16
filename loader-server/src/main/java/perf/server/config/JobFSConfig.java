package perf.server.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 2/2/13
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobFSConfig {
    private String jobStatsPath;
    private String jobResourceMonitoringFile;
    private String runJobMappingFile;
    private String runFile;
    private String jobRunNameFile;
    private String runsPath;
    private String jobsPath;

    public String getJobStatsPath() {
        return jobStatsPath;
    }

    public void setJobStatsPath(String jobStatsPath) {
        this.jobStatsPath = jobStatsPath;
    }

    public String getJobResourceMonitoringFile() {
        return jobResourceMonitoringFile;
    }

    public void setJobResourceMonitoringFile(String jobResourceMonitoringFile) {
        this.jobResourceMonitoringFile = jobResourceMonitoringFile;
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
}

