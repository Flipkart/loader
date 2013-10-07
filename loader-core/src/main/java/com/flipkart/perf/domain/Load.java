package com.flipkart.perf.domain;

import ch.qos.logback.classic.Level;
import com.flipkart.perf.core.LoadController;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Top Level Bean which is used to create Load configuration
 */
public class Load {
    private String logLevel = "INFO";
    private List<Group> groups;
    public Load() {
        this.groups = new ArrayList<Group>();
    }

    /**
     * Add group to your load work flow
     * @param group
     * @return
     */
    public Load addGroup(Group group) {
        this.groups.add(group);
        return this;
    }

    /**
     * Start Load
     * @throws Exception
     */
    public Load start(String jobId) throws Exception {
        this.setLogLevel();
        // Validate if anything is wrong with the Load Configuration
        validate();

        // Start the Load Controller and Wait for Completion
        LoadController loadController =  new LoadController(jobId, this);
        loadController.start();
        loadController.join();
        return this;
    }

    private void setLogLevel() {
        Level logLevel = Level.valueOf(this.logLevel);
        ch.qos.logback.classic.Logger root =  (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(logLevel);
    }

    /**
     * Validate Load Configuration
     */
    private void validate() {
        if(this.groups.size() == 0) {
            throw new RuntimeException("No Groups added to Load");
        }

        // Validate Each Group
        for(Group group : this.groups) {
            group.validate();
        }
    }

    public List<Group> getGroups() {
        return groups;
    }

    public Load setGroups(List<Group> groups) {
        this.groups = groups;
        return this;
    }

    public Map<String, Group> groupMap() {
        Map<String, Group>  groupMap = new HashMap<String, Group>();
        for(Group group : this.groups)
            groupMap.put(group.getName(), group);
        return groupMap;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
}
