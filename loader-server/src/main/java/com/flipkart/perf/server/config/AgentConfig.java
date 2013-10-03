package com.flipkart.perf.server.config;

public class AgentConfig {
    private int agentPort;
    private String agentsPath, agentInfoFile, agentPlatformLibInfoFile, agentClassLibInfoFile, jobLogUrl;

    public int getAgentPort() {
        return agentPort;
    }

    public AgentConfig setAgentPort(int agentPort) {
        this.agentPort = agentPort;
        return this;
    }

    public String getAgentsPath() {
        return agentsPath;
    }

    public AgentConfig setAgentsPath(String agentsPath) {
        this.agentsPath = agentsPath;
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

    public String getJobLogUrl(String jobId, String agentIp) {
        return jobLogUrl.
                replace("{jobId}", jobId).
                replace("{agentIp}", agentIp).
                replace("{port}", String.valueOf(agentPort));
    }

    public void setJobLogUrl(String jobLogUrl) {
        this.jobLogUrl = jobLogUrl;
    }
}
