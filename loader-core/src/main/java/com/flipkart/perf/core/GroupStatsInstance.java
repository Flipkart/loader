package com.flipkart.perf.core;

import com.flipkart.perf.util.Histogram;
import com.flipkart.perf.util.Timer;

import java.util.*;

/**
 * Keep stats collected in one iteration of SequentialFunctionExecutor
 */
public class GroupStatsInstance {
    private Map<String, Timer> customTimers;
    private Map<String, Timer> functionTimers; // Map of Function and (timestamp and execution time taken)
    private final Map<String, Histogram> functionHistograms;

    public GroupStatsInstance(Map<String, Timer> customTimers, Map<String, Timer> functionTimers, Map<String, Histogram> functionHistograms) {
        this.customTimers = customTimers;
        this.functionTimers = functionTimers;
        this.functionHistograms = functionHistograms;
    }

    public Map<String, Timer> getCustomTimers() {
        return customTimers;
    }

    public void addFunctionExecutionTime(String function, long executionTimeNS) {
        this.functionTimers.get(function).add(executionTimeNS);
    }


    public Map<String, Timer> getFunctionTimers() {
        return functionTimers;
    }

    public Map<String, Histogram> getFunctionHistograms() {
        return functionHistograms;
    }
}
