package perf.server.domain;

/**
 * Metric Publisher request is used by
 * monitoring client to register request with Monitoring service so that monitoring service can start publishing collected metrics
 */
public class MetricPublisherRequest {
    private String requestId;
    private MetricCollectionInfo collectionInfo;

    public MetricCollectionInfo getCollectionInfo() {
        return collectionInfo;
    }

    public MetricPublisherRequest setCollectionInfo(MetricCollectionInfo collectionInfo) {
        this.collectionInfo = collectionInfo;
        return this;
    }

    public String  getRequestId() {
        return requestId;
    }

    public MetricPublisherRequest setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }
}
