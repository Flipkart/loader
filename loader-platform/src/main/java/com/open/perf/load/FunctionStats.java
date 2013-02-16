package com.open.perf.load;

import com.open.perf.util.Counter;

import java.util.ArrayList;
import java.util.List;

public class FunctionStats {
	private String name;
    private Counter count;
    private Counter failureCounter;
    private Counter skipCounter;
    private Counter errorCounter;
    private List<Double> values;
    private double min=Double.MAX_VALUE, max=Double.MIN_VALUE;
    private double totalFunctionTime;

    public FunctionStats(String name) {
        this.name = name;
        this.count = new Counter(name);
        this.failureCounter = new Counter(name+"_failure");
        this.errorCounter = new Counter(name+"_error");
        this.skipCounter = new Counter(name+"_skip");
        this.values = new ArrayList<Double>();
        this.totalFunctionTime = 0d;
    }

    public FunctionStats executed() {
        this.count.increment();
        return this;
    }

    public FunctionStats failed() {
        this.failureCounter.increment();
        return this;
    }

    public FunctionStats errored() {
        this.errorCounter.increment();
        return this;
    }

    public FunctionStats skipped() {
        this.skipCounter.increment();
        return this;
    }

    public FunctionStats addValue(double functionTime) {
        this.values.add(functionTime);

        if(functionTime < this.min)
            this.min = functionTime;

        if(functionTime > this.max)
            this.max = functionTime;

        this.totalFunctionTime += functionTime;
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

    public List<Double> getValues() {
        return values;
    }

    public double getTotalFunctionTime() {
        return totalFunctionTime;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double computeAverage() {
        return this.getTotalFunctionTime() / this.values.size();
    }
}
