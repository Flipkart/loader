package com.flipkart.perf.server.config;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 2/5/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceChartConfig {
    private String resource;
    private List<ReportChartConfig> charts;

    public String getResource() {
        return resource;
    }

    public ResourceChartConfig setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public List<ReportChartConfig> getCharts() {
        return charts;
    }

    public ResourceChartConfig setCharts(List<ReportChartConfig> charts) {
        this.charts = charts;
        return this;
    }
}
