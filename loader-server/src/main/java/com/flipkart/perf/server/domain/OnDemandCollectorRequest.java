package com.flipkart.perf.server.domain;
import java.util.List;

public class OnDemandCollectorRequest {
    private List<OnDemandCollector> collectors;
    private String requestId;

    public List<OnDemandCollector> getCollectors() {
        return collectors;
    }

    public OnDemandCollectorRequest setCollectors(List<OnDemandCollector> collectors) {
        this.collectors = collectors;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public OnDemandCollectorRequest setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }
}
