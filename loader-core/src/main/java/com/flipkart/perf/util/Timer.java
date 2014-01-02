package com.flipkart.perf.util;
import com.flipkart.perf.common.util.TimeInstance;

import java.util.*;

/**
 * Allow you to capture time details of various functions.
 * Its thread safe. If same timer is used in different threads in parallel, this class takes care of that.
 * But user needs to make sure that startTimer and stopTimer is called in the same thread.
 */
public class Timer{
    private String timerName;
    private final String groupName;
    private final String functionName;
    private List<TimeInstance> timeList;

    public Timer(String groupName, String functionName, String timerName) {
        this.timerName = timerName;
        this.groupName = groupName;
        this.functionName = functionName;
        this.timeList = new LinkedList<TimeInstance>();
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

    public String getFunctionName() {
        return functionName;
    }

    public List getTimeList() {
        return timeList;
    }

    public long size() {
        return this.timeList.size();
    }

    public String toString() {
        return "Function Timer Name :"+this.timerName + " Samples :"+this.timeList.size();
    }

    synchronized public void add(long timeNS) {
        add(System.currentTimeMillis(), timeNS);
    }

    public void add(Map<Long, Long> timeList) {
        for(Long timeStamp : timeList.keySet())
            add(timeStamp, timeList.get(timeStamp));
    }

    synchronized public void add(long timeStampMS, long timeNS) {
        this.timeList.add(new TimeInstance(timeStampMS, timeNS));
    }
}
