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
    private Map<Long, Double> timeList;

    public Timer(String groupName, String timerName) {
        this.timerName = timerName;
        this.groupName = groupName;
        this.timeList = new LinkedHashMap<Long, Double>();
    }

    public TimerContext startTimer() {
        return new TimerContext(this);
    }

    public String getTimerName() {
        return timerName;
    }

    public String getGroupName() {
        return groupName;
    }

    public Map getTimeList() {
        return timeList;
    }

    public long size() {
        return this.timeList.size();
    }

    public String toString() {
        return "Function Timer Name :"+this.timerName + " Samples :"+this.timeList.size();
    }

    synchronized public void add(double time) {
        add(System.nanoTime(), time);
    }

    public void add(Map<Long, Double> timeList) {
        for(Long timeStamp : timeList.keySet())
            add(timeStamp, timeList.get(timeStamp));
    }

    synchronized public void add(long timeStamp, double time) {
        this.timeList.put(timeStamp, time);
    }
}
