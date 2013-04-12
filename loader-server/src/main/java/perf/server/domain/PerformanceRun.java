package perf.server.domain;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Represents a Performance Run
 */
public class PerformanceRun {
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

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PerformanceRun performanceRun = mapper.readValue(new File("/home/nitinka/git/loader2.0/loader-http-operations/sample/sampleGet.json"), PerformanceRun.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(performanceRun));
    }
}
