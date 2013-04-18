package perf.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 12/2/13
 * Time: 4:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoaderAgent {
    public static enum LoaderAgentStatus {
        ENABLED, BUSY, DISABLED, NOT_REACHABLE
    }

    private String ip;
    private Map<String,Object> attributes;
    private LoaderAgentStatus status;
    private List<String> runningJobs;

    public LoaderAgent() {}

    public LoaderAgent(String ip, Map<String,Object> agentAttributes) {
        this.ip = ip;
        this.attributes = agentAttributes;
        this.status = LoaderAgentStatus.ENABLED;
        this.runningJobs = new ArrayList<String>();
    }

    public String getIp() {
        return ip;
    }

    public LoaderAgent setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public LoaderAgent setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public LoaderAgentStatus getStatus() {
        return status;
    }

    public LoaderAgent setStatus(LoaderAgentStatus status) {
        this.status = status;
        return this;
    }

    public List<String> getRunningJobs() {
        return runningJobs;
    }

    public LoaderAgent setRunningJobs(List<String> runningJobs) {
        this.runningJobs = runningJobs;
        return this;
    }

    public LoaderAgent addRunningJob(String runningJob) {
        this.runningJobs.add(runningJob);
        return this;
    }

    public LoaderAgent setBusy() {
        this.status = LoaderAgentStatus.BUSY;
        return this;
    }

    public LoaderAgent setEnabled() {
        this.status = LoaderAgentStatus.ENABLED;
        return this;
    }

    public LoaderAgent setDisabled() {
        this.status = LoaderAgentStatus.DISABLED;
        return this;
    }

    public LoaderAgent setNotReachable() {
        this.status = LoaderAgentStatus.NOT_REACHABLE;
        return this;
    }
}
