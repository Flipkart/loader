package com.flipkart.perf.server.domain;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.flipkart.perf.common.util.FileHelper;

import com.flipkart.perf.server.config.JobFSConfig;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import com.flipkart.perf.server.util.ResponseBuilder;

import javax.ws.rs.WebApplicationException;

/**
 * Represents a Performance Run
 */
public class PerformanceRun {
    private String businessUnit = "default";
    private String team = "default";
    private String runName;
    private List<LoadPart> loadParts;
    private List<OnDemandMetricCollection> onDemandMetricCollections;
    private List<MetricCollection> metricCollections;

    public String getRunName() {
        return runName;
    }

    public PerformanceRun setRunName(String runName) {
        this.runName = runName;
        return this;
    }

    public List<LoadPart> getLoadParts() {
        return loadParts;
    }

    public PerformanceRun setLoadParts(List<LoadPart> loadParts) {
        this.loadParts = loadParts;
        return this;
    }

    public List<OnDemandMetricCollection> getOnDemandMetricCollections() {
        return onDemandMetricCollections;
    }

    public PerformanceRun setOnDemandMetricCollections(List<OnDemandMetricCollection> onDemandMetricCollections) {
        this.onDemandMetricCollections = onDemandMetricCollections;
        return this;
    }

    public List<MetricCollection> getMetricCollections() {
        return metricCollections;
    }

    public PerformanceRun setMetricCollections(List<MetricCollection> metricCollections) {
        this.metricCollections = metricCollections;
        return this;
    }

    public int agentsNeeded() {
        int totalAgentsNeeded = 0;
        for(LoadPart loadPart : this.getLoadParts()) {
            totalAgentsNeeded += loadPart.getAgents();
        }
        return totalAgentsNeeded;
    }

    public boolean exists() {
        JobFSConfig jobFSConfig = LoaderServerConfiguration.instance().getJobFSConfig();
        return new File(jobFSConfig.getRunPath(runName)).exists();
    }

    public void create() throws IOException {
        persist();
        BusinessUnit businessUnit = BusinessUnit.build(this.businessUnit);
        businessUnit.addRuns(this.team, Arrays.asList(new String[]{this.runName})).persist();
    }

    public void update(PerformanceRun newRun) throws IOException {
        BusinessUnit businessUnit = BusinessUnit.build(this.getBusinessUnit());
        businessUnit.getTeam(this.getTeam()).getRuns().remove(this.getRunName());
        businessUnit.persist();

        newRun.persist();
        businessUnit = BusinessUnit.build(newRun.businessUnit);
        businessUnit.addRuns(newRun.team, Arrays.asList(new String[]{newRun.runName})).persist();
    }

    public void persist() throws IOException {
        JobFSConfig jobFSConfig = LoaderServerConfiguration.instance().getJobFSConfig();
        String runFile = jobFSConfig.getRunFile(runName);
        FileHelper.createFilePath(runFile);
        ObjectMapperUtil.instance().writerWithDefaultPrettyPrinter().writeValue(new File(runFile), this);
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public PerformanceRun setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
        return this;
    }

    public String getTeam() {
        return team;
    }

    public PerformanceRun setTeam(String team) {
        this.team = team;
        return this;
    }

    public static PerformanceRun runExistsOrException(String runName) throws IOException {
        if(!new File(LoaderServerConfiguration.instance().getJobFSConfig().getRunPath(runName)).exists()) {
            throw new WebApplicationException(ResponseBuilder.runNameDoesNotExist(runName));
        }
        return ObjectMapperUtil.instance().
                readValue(new File(LoaderServerConfiguration.instance().getJobFSConfig().getRunFile(runName)), PerformanceRun.class);
    }

    public void delete() throws IOException {
        FileHelper.remove(LoaderServerConfiguration.instance().getJobFSConfig().getRunPath(runName));
        BusinessUnit businessUnit = BusinessUnit.build(this.getBusinessUnit());
        businessUnit.getTeam(this.team).getRuns().remove(this.runName);
        businessUnit.persist();
    }
}
