package com.flipkart.perf.domain;

import ch.qos.logback.classic.Level;
import com.flipkart.perf.common.jackson.ObjectMapperUtil;
import com.flipkart.perf.controller.JobController;
import com.flipkart.perf.core.LoadController;
import com.flipkart.perf.datagenerator.DataGeneratorInfo;
import com.flipkart.perf.inmemorydata.SharedDataInfo;
import com.strategicgains.restexpress.Format;
import com.strategicgains.restexpress.RestExpress;
import com.strategicgains.restexpress.response.ResponseProcessor;
import org.codehaus.jackson.JsonParseException;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Top Level Bean which is used to create Load configuration
 */
public class Load {
    private String logLevel = "INFO";
    private Group setupGroup;
    private List<Group> groups;
    private Group tearDownGroup;

    private static Logger logger = LoggerFactory.getLogger(Load.class);
    private Map<String, DataGeneratorInfo> dataGenerators;

    public Load() {
        this.groups = new ArrayList<Group>();
        this.dataGenerators = new LinkedHashMap<String, DataGeneratorInfo>();
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

    public Group getSetupGroup() {
        return setupGroup;
    }

    public void setSetupGroup(Group setupGroup) {
        this.setupGroup = setupGroup;
    }

    public Group getTearDownGroup() {
        return tearDownGroup;
    }

    public void setTearDownGroup(Group tearDownGroup) {
        this.tearDownGroup = tearDownGroup;
    }

    public Map<String, DataGeneratorInfo> getDataGenerators() {
        return dataGenerators;
    }

    public Load setDataGenerators(Map<String, DataGeneratorInfo> dataGenerators) {
        this.dataGenerators = dataGenerators;
        return this;
    }

    public Load addDataGenerator(DataGeneratorInfo dataGeneratorInfo) {
        this.dataGenerators.put(dataGeneratorInfo.getGeneratorName(), dataGeneratorInfo);
        return this;
    }

    public static void main(String[] args) throws Exception {
        Load load = ObjectMapperUtil.instance().readValue(new File("/tmp/1387349662547"), Load.class);
        load.start(System.currentTimeMillis()+"", 4567);
    }
}
