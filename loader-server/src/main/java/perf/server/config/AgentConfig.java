package perf.server.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 30/1/13
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgentConfig {
    private String platformLibResource;
    private String classLibResource;
    private String jobResource;
    private int agentPort;

    public String getPlatformLibResource() {
        return platformLibResource;
    }

    public void setPlatformLibResource(String platformLibResource) {
        this.platformLibResource = platformLibResource;
    }

    public String getClassLibResource() {
        return classLibResource;
    }

    public void setClassLibResource(String classLibResource) {
        this.classLibResource = classLibResource;
    }

    public String getJobResource() {
        return jobResource;
    }

    public void setJobResource(String jobResource) {
        this.jobResource = jobResource;
    }

    public int getAgentPort() {
        return agentPort;
    }

    public void setAgentPort(int agentPort) {
        this.agentPort = agentPort;
    }
}
