package perf.agent.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 25/10/12
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */

import com.yammer.dropwizard.config.Configuration;
import org.omg.PortableInterceptor.ServerRequestInfo;
import perf.agent.job.JobInfo;

import java.util.Map;

public class LoaderAgentConfiguration extends Configuration {
    private String appName;
    private ServerInfo serverInfo;
    private LibStorageConfig libStorageConfig;
    private JobProcessorConfig jobProcessorConfig;
    private JobStatSyncConfig jobStatSyncConfig;
    private Map registrationParams;

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

    public JobProcessorConfig getJobProcessorConfig() {
        return jobProcessorConfig;
    }

    public void setJobProcessorConfig(JobProcessorConfig jobProcessorConfig) {
        this.jobProcessorConfig = jobProcessorConfig;
    }

    public JobStatSyncConfig getJobStatSyncConfig() {
        return jobStatSyncConfig;
    }

    public void setJobStatSyncConfig(JobStatSyncConfig jobStatSyncConfig) {
        this.jobStatSyncConfig = jobStatSyncConfig;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public Map getRegistrationParams() {
        return registrationParams;
    }

    public void setRegistrationParams(Map registrationParams) {
        this.registrationParams = registrationParams;
    }
}
