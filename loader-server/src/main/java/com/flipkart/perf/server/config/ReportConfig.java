package com.flipkart.perf.server.config;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 2/5/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReportConfig {
    private Map<String, ResourceChartConfig> chartResources;

    public Map<String, ResourceChartConfig> getChartResources() {
        return chartResources;
    }

    public ReportConfig setChartResources(Map<String, ResourceChartConfig> chartResources) {
        this.chartResources = chartResources;
        return this;
    }
}
