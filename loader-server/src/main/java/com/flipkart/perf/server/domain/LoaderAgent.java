package com.flipkart.perf.server.domain;

import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.util.ObjectMapperUtil;

import java.io.File;
import java.io.IOException;
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
        FREE, BUSY, DISABLED, NOT_REACHABLE, D_REGISTERED
    }

    private String ip;
    private Map<String,Object> attributes;
    private LoaderAgentStatus status;
    private List<String> runningJobs;

    public LoaderAgent() {}

    public LoaderAgent(String ip, Map<String,Object> agentAttributes) {
        this.ip = ip;
        this.attributes = agentAttributes;
        this.status = LoaderAgentStatus.FREE;
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
        synchronized (this.runningJobs) {
            this.runningJobs.add(runningJob);
        }
        return this;
    }

    public LoaderAgent setFree() throws IOException {
        this.status = LoaderAgentStatus.FREE;
        persist();
        return this;
    }

    public LoaderAgent setBusy() throws IOException {
        this.status = LoaderAgentStatus.BUSY;
        persist();
        return this;
    }

    public LoaderAgent setEnabled() throws IOException {
        this.status = LoaderAgentStatus.FREE;
        persist();
        return this;
    }

    public LoaderAgent setDisabled() throws IOException {
        this.status = LoaderAgentStatus.DISABLED;
        persist();
        return this;
    }

    public LoaderAgent setNotReachable() throws IOException {
        this.status = LoaderAgentStatus.NOT_REACHABLE;
        persist();
        return this;
    }

    public LoaderAgent setDRegistered() throws IOException {
        this.status = LoaderAgentStatus.D_REGISTERED;
        persist();
        return this;
    }

    public LoaderAgent removeJob(String jobId) {
        synchronized (this.runningJobs) {
            runningJobs.remove(jobId);
        }
        return this;
    }

    public void persist() throws IOException {
        String agentInfoPath = LoaderServerConfiguration.instance().getAgentConfig().getAgentInfoFile(this.ip);
        FileHelper.createFilePath(agentInfoPath);
        ObjectMapperUtil.instance().writeValue(new File(agentInfoPath), this);
    }

}
