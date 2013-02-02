package perf.agent.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/1/13
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerInfo {
    private String baseUrl;
    private String jobFetchResource;
    private String jobStatsSyncResource;

    public String getBaseUrl() {
        return baseUrl;
    }

    public ServerInfo setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public String getJobFetchResource() {
        return jobFetchResource;
    }

    public ServerInfo setJobFetchResource(String jobFetchResource) {
        this.jobFetchResource = jobFetchResource;
        return this;
    }

    public String getJobStatsSyncResource() {
        return jobStatsSyncResource;
    }

    public ServerInfo setJobStatsSyncResource(String jobStatsSyncResource) {
        this.jobStatsSyncResource = jobStatsSyncResource;
        return this;
    }
}
