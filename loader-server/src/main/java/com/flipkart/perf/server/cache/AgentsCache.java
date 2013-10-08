package com.flipkart.perf.server.cache;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.perf.server.config.AgentConfig;
import com.flipkart.perf.server.domain.LoaderAgent;
import com.flipkart.perf.server.util.AgentHelper;
import com.flipkart.perf.server.util.ObjectMapperUtil;

public class AgentsCache {
    private static Map<String, LoaderAgent> agentInfoMap;
    private static AgentConfig agentConfig;
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();
    private static Logger logger = LoggerFactory.getLogger(AgentsCache.class);

    public static void initialize(AgentConfig agentConfig) {
        AgentsCache.agentConfig = agentConfig;
        agentInfoMap = new HashMap<String, LoaderAgent>();
        try {
            loadCache();
        } catch (IOException e) {
            logger.error("Error in Loading Agents information from file system",e);
        }
    }

    public static Map<String, LoaderAgent> getAgentInfoMap() {
        return agentInfoMap;
    }

    public static void setAgentInfoMap(Map<String, LoaderAgent> agentInfoMap) {
        AgentsCache.agentInfoMap = agentInfoMap;
    }

    public static void addAgent(LoaderAgent agent) throws IOException {
        AgentsCache.agentInfoMap.put(agent.getIp(), agent);
        agent.persist();
    }

    public static LoaderAgent getAgentInfo(String ip) {
        return AgentsCache.agentInfoMap.get(ip);
    }

    public static LoaderAgent removeAgent(String ip) {
        return AgentsCache.agentInfoMap.remove(ip);
    }

    /**
     * Load all agents information once server comes up
     * @throws IOException
     */
    private static void loadCache() throws IOException {
        File agentsPath = new File(agentConfig.getAgentsPath());
        if(agentsPath.exists()) {
            for(File agentPath : agentsPath.listFiles()) {
                File agentInfoFile = new File(agentConfig.getAgentInfoFile(agentPath.getName()));
                if(agentInfoFile.exists()) {
                    addAgent(objectMapper.readValue(agentInfoFile, LoaderAgent.class));
                }
            }
        }
    }

    public static List<LoaderAgent> freeAgents() throws IOException {
        List<LoaderAgent> freeAgents = new ArrayList<LoaderAgent>();
        for(LoaderAgent loaderAgent : agentInfoMap.values()) {
            if(loaderAgent.getStatus().equals(LoaderAgent.LoaderAgentStatus.FREE)) {
                AgentHelper.refreshAgentInfo(loaderAgent);
                if(loaderAgent.getStatus().equals(LoaderAgent.LoaderAgentStatus.FREE)) {
                    freeAgents.add(loaderAgent);
                }
            }
        }
        return freeAgents;
    }
}
