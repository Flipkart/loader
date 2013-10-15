package com.flipkart.perf.core;

import com.flipkart.perf.util.Counter;

/**
 * This class keep stats about a single function under execution
 */
public class FunctionCounter {
	private final String functionName;
    private final String groupName;
    private Counter count;
    private Counter failureCounter;
    private Counter skipCounter;
    private Counter errorCounter;
    private boolean ignore = false;

    public FunctionCounter(String groupName,String functionName) {
        this.functionName = functionName;
        this.groupName = groupName;
        this.count = new Counter(groupName, functionName, this.functionName +"_count");
        this.failureCounter = new Counter(groupName, functionName, this.functionName +"_failure");
        this.errorCounter = new Counter(groupName, functionName, this.functionName +"_error");
        this.skipCounter = new Counter(groupName, functionName, this.functionName +"_skip");
    }

    public FunctionCounter executed(int howMany) {
        if(!ignore)
            this.count.increment(howMany);
        return this;
    }

    public FunctionCounter executed() {
        if(!ignore)
            this.count.increment();
        return this;
    }

    public FunctionCounter failed(int howMany) {
        if(!ignore)
            this.failureCounter.increment(howMany);
        return this;
    }

    public FunctionCounter failed() {
        if(!ignore)
            this.failureCounter.increment();
        return this;
    }

    public FunctionCounter errored(int howMany) {
        if(!ignore)
            this.errorCounter.increment(howMany);
        return this;
    }

    public FunctionCounter errored() {
        if(!ignore)
            this.errorCounter.increment();
        return this;
    }

    public FunctionCounter skipped(int howMany) {
        if(!ignore)
            this.skipCounter.increment(howMany);
        return this;
    }

    public FunctionCounter skipped() {
        if(!ignore)
            this.skipCounter.increment();
        return this;
    }

    public String getFunctionName() {
        return functionName;
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

    public boolean isIgnore() {
        return ignore;
    }

    public void ignore() {
        this.ignore = true;
    }

    public String toString() {
        return "\nExecutions :" + this.getCount() + "\n"
                + "Errors :" + this.getErrorCounter() + "\n"
                + "Skips :" + this.getSkipCounter() + "\n"
                + "Failures :" + this.getFailureCounter() + "\n";
    }

}
