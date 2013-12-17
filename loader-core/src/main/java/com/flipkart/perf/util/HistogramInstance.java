package com.flipkart.perf.util;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 8/10/13
 * Time: 12:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class HistogramInstance {
    private long atTime;
    private double value;

    public HistogramInstance(long atTime, double value) {
        this.atTime = atTime;
        this.value = value;
    }

    public long getAtTime() {
        return atTime;
    }

    public void setAtTime(long atTime) {
        this.atTime = atTime;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
