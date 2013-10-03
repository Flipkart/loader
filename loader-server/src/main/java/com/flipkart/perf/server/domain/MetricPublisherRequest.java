package com.flipkart.perf.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

/**
 * Metric Publisher request is used by
 * monitoring client to register request with Monitoring service so that monitoring service can start publishing collected metrics
 */
public class MetricPublisherRequest {
    private String requestId;
    private Set<String> resources;
    private int lastHowManyInstances;
    private String publishUrl; // post url
    private long forHowLong; // ms
    private long interval = 60000; // ms
    private long startTime = -1;

    public String  getRequestId() {
        return requestId;
    }

    public MetricPublisherRequest setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public Set<String> getResources() {
        return resources;
    }

    public MetricPublisherRequest setResources(Set<String> resources) {
        this.resources = resources;
        return this;
    }

    public int getLastHowManyInstances() {
        return lastHowManyInstances;
    }

    public MetricPublisherRequest setLastHowManyInstances(int lastHowManyInstances) {
        this.lastHowManyInstances = lastHowManyInstances;
        return this;
    }

    public String getPublishUrl() {
        return publishUrl;
    }

    public MetricPublisherRequest setPublishUrl(String publishUrl) {
        this.publishUrl = publishUrl;
        return this;
    }

    public long getForHowLong() {
        return forHowLong;
    }

    public MetricPublisherRequest setForHowLong(long forHowLong) {
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

    public MetricPublisherRequest setInterval(long interval) {
        this.interval = interval;
        return this;
    }
}
