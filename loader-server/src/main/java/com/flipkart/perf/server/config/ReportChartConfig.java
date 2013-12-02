package com.flipkart.perf.server.config;

import java.util.List;

public class ReportChartConfig {
    private String title;
    private String xLegend;
    private String yLegend;

    private List<GraphKeys> keysToPlot;

    public String getTitle() {
        return title;
    }

    public ReportChartConfig setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getxLegend() {
        return xLegend;
    }

    public ReportChartConfig setxLegend(String xLegend) {
        this.xLegend = xLegend;
        return this;
    }

    public String getyLegend() {
        return yLegend;
    }

    public ReportChartConfig setyLegend(String yLegend) {
        this.yLegend = yLegend;
        return this;
    }

    public List<GraphKeys> getKeysToPlot() {
        return keysToPlot;
    }

    public ReportChartConfig setKeysToPlot(List<GraphKeys> keysToPlot) {
        this.keysToPlot = keysToPlot;
        return this;
    }
}
