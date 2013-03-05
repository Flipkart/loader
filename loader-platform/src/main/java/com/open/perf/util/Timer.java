package com.open.perf.util;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 14/2/13
 * Time: 10:17 AM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Allow you to capture time details of various functions.
 * Its thread safe. If same timer is used in different threads in parallel, this class takes care of that.
 * But user needs to make sure that startTimer and stopTimer is called in the same thread.
 */
public class Timer{
    private String timerName;
    private final String groupName;
    private Double min = Double.MAX_VALUE;
    private Double max = 0d;
    private Map<Long, Double> timeList;
    private Double totalTime; // Microseconds

    public Timer(String groupName, String timerName) {
        this.timerName = timerName;
        this.groupName = groupName;
        this.timeList = new LinkedHashMap<Long, Double>();
        this.totalTime = 0d;
    }

    public TimerContext startTimer() {
        return new TimerContext(this);
    }

    public String getTimerName() {
        return timerName;
    }

    public Map getTimeList() {
        return timeList;
    }

    public long size() {
        return this.timeList.size();
    }

    public String toString() {
        return "Function Timer Name :"+this.timerName + " Samples :"+this.timeList.size()+" Min :"+this.min +" Max :"+this.max;
    }

    synchronized public void add(double time) {
        add(System.nanoTime(), time);
    }

    public void add(Map<Long, Double> timeList) {
        for(Long timeStamp : timeList.keySet())
            add(timeStamp, timeList.get(timeStamp));
    }

    synchronized public void add(long timeStamp, double time) {
        time = time/1000d;
        this.timeList.put(timeStamp, time);
        if(time < this.min)
            this.min = time;

        if(time > this.max)
            this.max = time;
        this.totalTime += time;
    }

    public Double totalTime() {
        return this.totalTime;
    }

    public Double min() {
        return min;
    }

    public Double max() {
        return max;
    }

    public Double getTotalTime() {
        return this.totalTime;
    }
}
