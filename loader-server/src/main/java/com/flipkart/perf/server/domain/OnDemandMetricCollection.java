package com.flipkart.perf.server.domain;

import java.util.List;

/**
 * Represent Instance on OnDemand Metric Collection in Performance un
 */
public class OnDemandMetricCollection {
    private String agent;
    private List<OnDemandCollector> collectors;

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public List<OnDemandCollector> getCollectors() {
        return collectors;
    }

    public void setCollectors(List<OnDemandCollector> collectors) {
        this.collectors = collectors;
    }

    public OnDemandCollectorRequest buildRequest(String jobId) {
        return new OnDemandCollectorRequest().
                setRequestId(jobId).
                setCollectors(collectors);
    }
}
