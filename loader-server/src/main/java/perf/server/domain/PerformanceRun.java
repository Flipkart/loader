package perf.server.domain;


import java.io.File;
import java.io.IOException;
import java.util.List;

import com.open.perf.util.FileHelper;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;

import perf.server.config.JobFSConfig;
import perf.server.config.LoaderServerConfiguration;
import perf.server.util.ObjectMapperUtil;
import perf.server.util.ResponseBuilder;

import javax.ws.rs.WebApplicationException;

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

    public int agentsNeeded() {
        int totalAgentsNeeded = 0;
        for(LoadPart loadPart : this.getLoadParts()) {
            totalAgentsNeeded += loadPart.getAgents();
        }
        return totalAgentsNeeded;
    }

    public boolean exists() {
        JobFSConfig jobFSConfig = LoaderServerConfiguration.instance().getJobFSConfig();
        return new File(jobFSConfig.getRunPath(runName)).exists();
    }

    public void persist() throws IOException {
        JobFSConfig jobFSConfig = LoaderServerConfiguration.instance().getJobFSConfig();
        String runFile = jobFSConfig.getRunFile(runName);
        FileHelper.createFilePath(runFile);
        ObjectMapperUtil.instance().writerWithDefaultPrettyPrinter().writeValue(new File(runFile), this);
    }

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = ObjectMapperUtil.instance();
        PerformanceRun performanceRun = mapper.readValue(new File("/home/nitinka/git/loader2.0/loader-http-operations/sample/sampleGet.json"), PerformanceRun.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(performanceRun));
    }
}
