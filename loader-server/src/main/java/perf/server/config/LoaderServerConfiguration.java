package perf.server.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 25/10/12
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */

import com.yammer.dropwizard.config.Configuration;

public class LoaderServerConfiguration extends Configuration {
    private String appName;
    private LibStorageFSConfig libStorageFSConfig;
    private AgentConfig agentConfig;
    private MonitoringAgentConfig monitoringAgentConfig;
    private JobFSConfig jobFSConfig;
    private String reportConfigFile;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public LibStorageFSConfig getLibStorageFSConfig() {
        return libStorageFSConfig;
    }

    public void setLibStorageFSConfig(LibStorageFSConfig libStorageFSConfig) {
        this.libStorageFSConfig = libStorageFSConfig;
    }

    public AgentConfig getAgentConfig() {
        return agentConfig;
    }

    public void setAgentConfig(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }

    public JobFSConfig getJobFSConfig() {
        return jobFSConfig;
    }

    public void setJobFSConfig(JobFSConfig jobFSConfig) {
        this.jobFSConfig = jobFSConfig;
    }

    public MonitoringAgentConfig getMonitoringAgentConfig() {
        return monitoringAgentConfig;
    }

    public void setMonitoringAgentConfig(MonitoringAgentConfig monitoringAgentConfig) {
        this.monitoringAgentConfig = monitoringAgentConfig;
    }

    public String getReportConfigFile() {
        return reportConfigFile;
    }

    public LoaderServerConfiguration setReportConfigFile(String reportConfigFile) {
        this.reportConfigFile = reportConfigFile;
        return this;
    }
}


