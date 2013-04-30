package perf.agent.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 25/10/12
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */

import com.yammer.dropwizard.config.Configuration;
import perf.agent.util.SystemInfoHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class LoaderAgentConfiguration extends Configuration {
    private String appName;
    private ServerInfo serverInfo;
    private LibStorageConfig libStorageConfig;
    private JobProcessorConfig jobProcessorConfig;
    private JobFSConfig jobFSConfig;
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
        try {
            this.registrationParams.putAll(SystemInfoHelper.getOSParams());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public JobFSConfig getJobFSConfig() {
        return jobFSConfig;
    }

    public void setJobFSConfig(JobFSConfig jobFSConfig) {
        this.jobFSConfig = jobFSConfig;
    }
}
