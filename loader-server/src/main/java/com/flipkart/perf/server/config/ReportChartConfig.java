package com.flipkart.perf.server.config;

import java.util.List;

public class ReportChartConfig {
    private String title;
    private List<String> keysToPlot;

    public String getTitle() {
        return title;
    }

    public ReportChartConfig setTitle(String title) {
        this.title = title;
        return this;
    }

    public List<String> getKeysToPlot() {
        return keysToPlot;
    }

    public ReportChartConfig setKeysToPlot(List<String> keysToPlot) {
        this.keysToPlot = keysToPlot;
        return this;
    }
}
