package com.flipkart.perf.server.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents one instance of stats collected for any resource
 */
public class ResourceCollectionInstance {
    private String resourceName;
    private Map<String,Double> metrics = new HashMap<String,Double>();
    private Long time;

    public String getResourceName() {
        return resourceName;
    }

    public ResourceCollectionInstance setResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public Long getTime() {
        return time;
    }

    public ResourceCollectionInstance setTime(Long time) {
        this.time = time;
        return this;
    }

    public Map<String, Double> getMetrics() {
        return metrics;
    }

    public ResourceCollectionInstance setMetrics(Map<String, Double> metrics) {
        this.metrics = metrics;
        return this;
    }

    public ResourceCollectionInstance addMetrics(Map<String, Double> metrics) {
        this.metrics.putAll(metrics);
        return this;
    }

    public ResourceCollectionInstance addMetric(String metricName, Double metricValue) {
        this.metrics.put(metricName, metricValue);
        return this;
    }

    public String toString() {
        return "Resource :" + resourceName + " Metrics :" + metrics + "Time :" + time;
    }
}
