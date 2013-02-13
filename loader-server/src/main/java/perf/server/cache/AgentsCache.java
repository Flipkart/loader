package perf.server.cache;

import perf.server.domain.LoaderAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 12/2/13
 * Time: 4:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgentsCache {
    private static Map<String, LoaderAgent> agentInfoMap;
    static {
        agentInfoMap = new HashMap<String, LoaderAgent>();
    }

    public static Map<String, LoaderAgent> getAgentInfoMap() {
        return agentInfoMap;
    }

    public static void setAgentInfoMap(Map<String, LoaderAgent> agentInfoMap) {
        AgentsCache.agentInfoMap = agentInfoMap;
    }

    public static void addAgentInfoMap(LoaderAgent agent) {
        AgentsCache.agentInfoMap.put(agent.getIp(), agent);
    }

    public static LoaderAgent getAgentInfo(String ip) {
        return AgentsCache.agentInfoMap.get(ip);
    }

    public static LoaderAgent removeAgentInfo(String ip) {
        return AgentsCache.agentInfoMap.remove(ip);
    }
}
