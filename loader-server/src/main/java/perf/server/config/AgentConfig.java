package perf.server.config;

public class AgentConfig {
    private int agentPort;
    private String agentInfoFile, agentPlatformLibInfoFile, agentClassLibInfoFile;

    public int getAgentPort() {
        return agentPort;
    }

    public AgentConfig setAgentPort(int agentPort) {
        this.agentPort = agentPort;
        return this;
    }

    public String getAgentInfoFile(String agentIp) {
        return agentInfoFile.replace("{agentIp}", agentIp);
    }

    public AgentConfig setAgentInfoFile(String agentInfoFile) {
        this.agentInfoFile = agentInfoFile;
        return this;
    }

    public String getAgentPlatformLibInfoFile(String agentIp) {
        return agentPlatformLibInfoFile.replace("{agentIp}", agentIp);
    }

    public AgentConfig setAgentPlatformLibInfoFile(String agentPlatformLibInfoFile) {
        this.agentPlatformLibInfoFile = agentPlatformLibInfoFile;
        return this;
    }

    public String getAgentClassLibInfoFile(String agentIp) {
        return agentClassLibInfoFile.replace("{agentIp}", agentIp);
    }

    public AgentConfig setAgentClassLibInfoFile(String agentClassLibInfoFile) {
        this.agentClassLibInfoFile = agentClassLibInfoFile;
        return this;
    }
}
