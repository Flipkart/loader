package com.flipkart.perf.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

public class MetricCollectionInfo {
    private Set<String> resources;
    private int lastHowManyInstances;
    private String publishUrl; // post url : Generally Loader Server url/jobs/jobId/monitoringStats
    private long forHowLong; // ms
    private long interval = 60000; // ms
    private long startTime = -1;

    public Set<String> getResources() {
        return resources;
    }

    public MetricCollectionInfo setResources(Set<String> resources) {
        this.resources = resources;
        return this;
    }

    public int getLastHowManyInstances() {
        return lastHowManyInstances;
    }

    public MetricCollectionInfo setLastHowManyInstances(int lastHowManyInstances) {
        this.lastHowManyInstances = lastHowManyInstances;
        return this;
    }

    public String getPublishUrl() {
        return publishUrl;
    }

    public MetricCollectionInfo setPublishUrl(String publishUrl) {
        this.publishUrl = publishUrl;
        return this;
    }

    public long getForHowLong() {
        return forHowLong;
    }

    public MetricCollectionInfo setForHowLong(long forHowLong) {
        this.forHowLong = forHowLong;
        return this;
    }

    @JsonIgnore
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }
}