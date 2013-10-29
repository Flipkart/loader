package com.flipkart.perf.server.config;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 29/10/13
 * Time: 10:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class SystemStatsArchivalConfig {
    private String archivalEngineClass;
    private Map<String, Object> configParams;

    public String getArchivalEngineClass() {
        return archivalEngineClass;
    }

    public void setArchivalEngineClass(String archivalEngineClass) {
        this.archivalEngineClass = archivalEngineClass;
    }

    public Map<String, Object> getConfigParams() {
        return configParams;
    }

    public void setConfigParams(Map<String, Object> configParams) {
        this.configParams = configParams;
    }
}
