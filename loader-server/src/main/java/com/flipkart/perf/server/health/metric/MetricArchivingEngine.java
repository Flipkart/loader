package com.flipkart.perf.server.health.metric;

import com.flipkart.perf.common.util.ClassHelper;
import com.flipkart.perf.server.config.SystemStatsArchivalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

abstract public class MetricArchivingEngine{
    private Map<String, Object> config;
    protected static Logger logger = LoggerFactory.getLogger(MetricArchivingEngine.class);

    public MetricArchivingEngine(Map<String, Object> config) {
        this.config = config;
    }

    final public void archive(ResourceMetric resourceMetric) throws IOException {
        archive(Arrays.asList(new ResourceMetric[]{resourceMetric}));
    }
    abstract public void archive(List<ResourceMetric> resourceMetrics) throws IOException;
    abstract public List<String> metrics() throws IOException;
    abstract public String fetchMetrics(String metricName, String consolFuc, long startTimeSec, long endTimeSec) throws IOException;
    abstract public InputStream fetchMetricsImage(String metricName, String consolFuc, long startTimeSec, long endTimeSec) throws IOException;

    public static final MetricArchivingEngine build(SystemStatsArchivalConfig config) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return ClassHelper.getClassInstance(config.getArchivalEngineClass(),
                new Class[]{Map.class},
                new Object[]{config.getConfigParams()},
                MetricArchivingEngine.class);
    }
}
