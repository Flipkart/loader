package com.flipkart.perf.server.health.metric;

import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

abstract public class MetricMonitor {

    private String name;
    private int interval=10000;

    public MetricMonitor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInterval() {
        return interval;
    }

    public MetricMonitor setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    abstract public ResourceMetric get() throws IOException, MalformedObjectNameException, ExecutionException;
}
