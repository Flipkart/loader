package loader.monitor.collector;

import loader.monitor.domain.Metric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents one instance of stats collected for any resource
 */
public class ResourceCollectionInstance {

    private String resourceName;
    private List<Metric> metrics = new ArrayList<Metric>();
    private Long time;

    public String getResourceName() {
        return resourceName;
    }

    public ResourceCollectionInstance setResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public ResourceCollectionInstance setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
        return this;
    }

    public ResourceCollectionInstance addMetric(Metric metric) {
        this.metrics.add(metric);
        return this;
    }

    public Long getTime() {
        return time;
    }

    public ResourceCollectionInstance setTime(Long time) {
        this.time = time;
        return this;
    }

    public String toString() {
        return "Resource :" + resourceName + " Metrics :" + metrics + "Time :" + time;
    }

    public ResourceCollectionInstance addMetrics(List<Metric> metrics) {
        this.metrics.addAll(metrics);
        return this;
    }
}
