package com.flipkart.perf.server.health.metric;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MetricArchiverQueue {
    private static LinkedBlockingQueue<ResourceMetric> resourceMetricQueue;
    static {
        resourceMetricQueue = new LinkedBlockingQueue<ResourceMetric>(100000);
    }

    public static void offer(ResourceMetric resourceMetric) {
        resourceMetricQueue.offer(resourceMetric);
    }

    public static void offer(String metricName, double metricValue) {
        resourceMetricQueue.offer(new ResourceMetric().addMetrics(metricName, metricValue, Metric.MetricType.GAUGE));
    }

    public static void offer(String metricName, double metricValue, Metric.MetricType metricType) {
        resourceMetricQueue.offer(new ResourceMetric().addMetrics(metricName, metricValue, metricType));
    }

    public static ResourceMetric poll() throws InterruptedException {
        return resourceMetricQueue.poll(1, TimeUnit.SECONDS);
    }
}
