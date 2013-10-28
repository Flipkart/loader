package com.flipkart.perf.server.domain;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 25/1/13
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class OnDemandCollector {
    private String name;
    private String klass;
    private int interval;
    private Map<String,Object> params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKlass() {
        return klass;
    }

    public void setKlass(String klass) {
        this.klass = klass;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}