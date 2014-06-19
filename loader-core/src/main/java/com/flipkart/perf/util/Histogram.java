package com.flipkart.perf.util;

import com.flipkart.perf.common.util.Clock;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 8/10/13
 * Time: 12:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class Histogram {
    private String groupName, functionName, name;
    private List<HistogramInstance> instances;

    public Histogram(String groupName, String functionName, String name) {
        this.groupName = groupName;
        this.functionName = functionName;
        this.name = name;
        this.instances = new LinkedList<HistogramInstance>();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getName() {
        return name;
    }

    public List<HistogramInstance> getInstances() {
        return instances;
    }

    public void addValue(double value) {
        this.instances.add(new HistogramInstance(Clock.milliTick(), value));
    }
}
