package perf.server.domain;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 25/1/13
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
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
