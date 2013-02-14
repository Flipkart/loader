package com.open.perf.operation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 14/2/13
 * Time: 10:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class FunctionTimer implements Cloneable{
    private String timerName;
    private Double minTime = Double.MAX_VALUE;
    private Double maxTime = 0d;
    private Long startTime;
    private List<Double> timeList;

    public FunctionTimer(String timerName) {
        this.timerName = timerName;
        this.timeList = new ArrayList<Double>();
    }

    public FunctionTimer startTimer() {
        this.startTime = System.nanoTime();
        return this;
    }

    public double endTimer() {
        if(this.startTime == null)
            throw new RuntimeException("Timer "+this.timerName+" Not started");
        double timeTaken = (System.nanoTime() - this.startTime)/1000000;
        timeList.add(timeTaken);

        if(timeTaken < minTime)
            minTime = timeTaken;

        if(timeTaken > maxTime)
            maxTime = timeTaken;
        return timeTaken;
    }

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public List<Double> getTimeList() {
        return timeList;
    }

    public String toString() {
        return "Function Timer Name :"+this.timerName + " Samples :"+this.timeList.size()+" Min :"+this.minTime+" Max :"+this.maxTime;
    }
}
