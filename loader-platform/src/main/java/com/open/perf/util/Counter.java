package com.open.perf.util;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 14/2/13
 * Time: 10:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class Counter {
    private String counterName;
    private long count;

    public Counter(String counterName) {
        this.counterName = counterName;
        count = 0;
    }

    public String getCounterName() {
        return counterName;
    }

    synchronized public Counter increment() {
        count++;
        return this;
    }

    synchronized public Counter increment(int by) {
        count += by;
        return this;
    }

    synchronized public Counter decrement() {
        count--;
        return this;
    }

    synchronized public Counter decrement(int by) {
        count -= by;
        return this;
    }

    public long count() {
        return count;
    }

    synchronized public Counter reset() {
        count = 0;
        return this;
    }


    @Override
    public String toString() {
        return "Function Counter Name : "+this.counterName+" counter : "+this.count;
    }
}
