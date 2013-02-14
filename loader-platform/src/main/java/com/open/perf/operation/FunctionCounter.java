package com.open.perf.operation;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 14/2/13
 * Time: 10:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class FunctionCounter {
    private String counterName;
    private long count;

    public FunctionCounter(String counterName) {
        this.counterName = counterName;
        count = 0;
    }

    public String getCounterName() {
        return counterName;
    }

    synchronized public FunctionCounter increment() {
        count++;
        return this;
    }

    synchronized public FunctionCounter increment(int by) {
        count += by;
        return this;
    }

    synchronized public FunctionCounter decrement() {
        count--;
        return this;
    }

    synchronized public FunctionCounter decrement(int by) {
        count -= by;
        return this;
    }

    public long getCurrentCount() {
        return count;
    }

    synchronized public FunctionCounter reset() {
        count = 0;
        return this;
    }


    @Override
    public String toString() {
        return "Function Counter Name : "+this.counterName+" counter : "+this.count;
    }
}
