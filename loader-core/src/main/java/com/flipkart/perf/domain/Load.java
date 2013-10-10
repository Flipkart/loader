package com.flipkart.perf.domain;

import ch.qos.logback.classic.Level;
import com.flipkart.perf.controller.JobController;
import com.flipkart.perf.core.LoadController;
import com.strategicgains.restexpress.Format;
import com.strategicgains.restexpress.RestExpress;
import com.strategicgains.restexpress.response.ResponseProcessor;
import org.apache.commons.cli.CommandLine;
import org.jboss.netty.handler.codec.http.HttpMethod;
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
    private static Logger logger = LoggerFactory.getLogger(Load.class);

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
    public Load start(String jobId, int httpPort) throws Exception {
        this.setLogLevel();
        // Validate if anything is wrong with the Load Configuration
        validate();
        RestExpress server = initializeHttpServer(httpPort);
        try {
            // Start the Load Controller and Wait for Completion
            LoadController loadController =  new LoadController(jobId, this);
            loadController.start();
            loadController.join();
        }
        catch (Exception e) {
            logger.error("Error While Generating Load", e);
            throw e;
        }
        finally {
            server.shutdown();
        }
        return this;
    }

    private static RestExpress initializeHttpServer(int httpPort) {
        RestExpress server = new RestExpress();
        server.putResponseProcessor(Format.JSON, ResponseProcessor.defaultJsonProcessor());
        server.uri("/loader-core/kill", new JobController()).action("kill", HttpMethod.PUT);
        server.bind(httpPort);
        return server;
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
