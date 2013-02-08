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
    private LibStorageConfig libStorageConfig;
    private AgentConfig agentConfig;
    private MonitoringAgentConfig monitoringAgentConfig;
    private JobStatsConfig jobStatsConfig;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public LibStorageConfig getLibStorageConfig() {
        return libStorageConfig;
    }

    public void setLibStorageConfig(LibStorageConfig libStorageConfig) {
        this.libStorageConfig = libStorageConfig;
    }

    public AgentConfig getAgentConfig() {
        return agentConfig;
    }

    public void setAgentConfig(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }

    public JobStatsConfig getJobStatsConfig() {
        return jobStatsConfig;
    }

    public void setJobStatsConfig(JobStatsConfig jobStatsConfig) {
        this.jobStatsConfig = jobStatsConfig;
    }

    public MonitoringAgentConfig getMonitoringAgentConfig() {
        return monitoringAgentConfig;
    }

    public void setMonitoringAgentConfig(MonitoringAgentConfig monitoringAgentConfig) {
        this.monitoringAgentConfig = monitoringAgentConfig;
    }
}
