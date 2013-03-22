package com.open.perf.util;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 22/3/13
 * Time: 4:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeInstance {
    private long atTime;
    private double howMuchTime;

    public TimeInstance(long atTime, double howMuchTime) {
        this.atTime = atTime;
        this.howMuchTime = howMuchTime;
    }

    public long getAtTime() {
        return atTime;
    }

    public TimeInstance setAtTime(long atTime) {
        this.atTime = atTime;
        return this;
    }

    public double getHowMuchTime() {
        return howMuchTime;
    }

    public TimeInstance setHowMuchTime(long howMuchTime) {
        this.howMuchTime = howMuchTime;
        return this;
    }
}
