package com.flipkart.perf.server.domain;

/**
 * Represents what metrics to collect and from which monitoring agent
 */
public class MetricCollection {
    private String agent;
    private MetricCollectionInfo collectionInfo;

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public MetricCollectionInfo getCollectionInfo() {
        return collectionInfo;
    }

    public void setCollectionInfo(MetricCollectionInfo collectionInfo) {
        this.collectionInfo = collectionInfo;
    }

    public MetricPublisherRequest buildRequest(String jobId) {
        return new MetricPublisherRequest().
                setRequestId(jobId).
                setForHowLong(collectionInfo.getForHowLong()).
                setLastHowManyInstances(collectionInfo.getLastHowManyInstances()).
                setPublishUrl(collectionInfo.getPublishUrl().replace("{jobId}", jobId)).
                setResources(collectionInfo.getResources()).
                setInterval(collectionInfo.getInterval());
    }
}