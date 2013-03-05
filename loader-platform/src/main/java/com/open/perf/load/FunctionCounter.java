package com.open.perf.load;

import com.open.perf.util.Counter;

public class FunctionCounter {
	private final String name;
    private final String groupName;
    private Counter count;
    private Counter failureCounter;
    private Counter skipCounter;
    private Counter errorCounter;

    public FunctionCounter(String groupName, String name) {
        this.name = name;
        this.groupName = groupName;
        this.count = new Counter(name);
        this.failureCounter = new Counter(name+"_failure");
        this.errorCounter = new Counter(name+"_error");
        this.skipCounter = new Counter(name+"_skip");
    }

    public FunctionCounter executed(Integer integer) {
        this.count.increment(integer);
        return this;
    }

    public FunctionCounter executed() {
        this.count.increment();
        return this;
    }

    public FunctionCounter failed(Integer integer) {
        this.failureCounter.increment(integer);
        return this;
    }

    public FunctionCounter failed() {
        this.failureCounter.increment();
        return this;
    }

    public FunctionCounter errored(Integer integer) {
        this.errorCounter.increment(integer);
        return this;
    }

    public FunctionCounter errored() {
        this.errorCounter.increment();
        return this;
    }

    public FunctionCounter skipped(Integer integer) {
        this.skipCounter.increment(integer);
        return this;
    }

    public FunctionCounter skipped() {
        this.skipCounter.increment();
        return this;
    }

    public String getName() {
        return name;
    }

    public Counter getCount() {
        return count;
    }

    public Counter getFailureCounter() {
        return failureCounter;
    }

    public Counter getSkipCounter() {
        return skipCounter;
    }

    public Counter getErrorCounter() {
        return errorCounter;
    }

    public String getGroupName() {
        return groupName;
    }

    public String toString() {
        return "\nExecutions :" + this.getCount() + "\n"
                + "Errors :" + this.getErrorCounter() + "\n"
                + "Skips :" + this.getSkipCounter() + "\n"
                + "Failures :" + this.getFailureCounter() + "\n";
    }

}
