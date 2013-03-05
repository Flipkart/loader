package com.open.perf.load;

import com.open.perf.util.Timer;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 20/2/13
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class GroupStatsInstance {
    private Map<String, Timer> customTimers;
    private Map<String, Timer> functionTimers; // Map of Function end timestamp and execution time taken
    private final long startTime;
    private long endTime;

    public GroupStatsInstance(Map<String, Timer> customTimers, Map<String,Timer> functionTimers) {
        this.startTime = System.nanoTime();
        this.customTimers = customTimers;
        this.functionTimers = functionTimers;
    }

    public Map<String, Timer> getCustomTimers() {
        return customTimers;
    }

    public void addFunctionExecutionTime(String function, double executionTime) {
        this.functionTimers.get(function).add(executionTime);
    }


    public Map<String, Timer> getFunctionTimers() {
        return functionTimers;
    }

    public GroupStatsInstance setEndTime(long time) {
        this.endTime = time;
        return this;
    }

    @Override
    public String toString() {
        return "Timers :" + this.customTimers +"\n"
                + "Function Execution Times :" + this.functionTimers + "\n";
    }
}
