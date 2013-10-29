package com.flipkart.perf.server.health.metric;

import com.flipkart.perf.server.util.ObjectMapperUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DummyArchivingEngine extends MetricArchivingEngine{

    public DummyArchivingEngine(Map<String, Object> config) {
        super(config);
    }

    @Override
    public void archive(List<ResourceMetric> resourceMetrics) throws IOException {
        System.out.println("Printing Metrics "+ ObjectMapperUtil.instance().defaultPrettyPrintingWriter().writeValueAsString(resourceMetrics));
    }

    @Override
    public List<String> metrics() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String fetchMetrics(String metricName, String consolFuc, long startTimeSec, long endTimeSec) throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InputStream fetchMetricsImage(String metricName, String consolFuc, long startTimeSec, long endTimeSec) throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
