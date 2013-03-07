package com.open.perf.core;

import com.open.perf.util.Timer;

import java.util.*;

/**
 * Keep stats collected in one iteration of SequentialFunctionExecutor
 */
public class GroupStatsInstance {
    private Map<String, Timer> customTimers;
    private Map<String, Timer> functionTimers; // Map of Function end timestamp and execution time taken

    public GroupStatsInstance(Map<String, Timer> customTimers, Map<String,Timer> functionTimers) {
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

    @Override
    public String toString() {
        return "Timers :" + this.customTimers +"\n"
                + "Function Execution Times :" + this.functionTimers + "\n";
    }
}
