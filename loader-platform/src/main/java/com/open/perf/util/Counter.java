package com.open.perf.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * nitinka
 */
public class Counter {
    private String counterName;
    private long initialCount;
    private AtomicLong count;
    private long lastUpdateTime;
    public Counter(String counterName) {
        this(counterName, 0);
        this.lastUpdateTime = System.nanoTime();
    }

    public Counter(String counterName, long initialCount) {
        this.counterName = counterName;
        count = new AtomicLong(initialCount);
        this.initialCount = initialCount;
    }

    public String getCounterName() {
        return counterName;
    }

    synchronized public long increment() {
        this.lastUpdateTime = System.nanoTime();
        return count.incrementAndGet();
    }

    synchronized public long increment(long by) {
        this.lastUpdateTime = System.nanoTime();
        return count.addAndGet(by);
    }

    synchronized public long decrement() {
        this.lastUpdateTime = System.nanoTime();
        return count.decrementAndGet();
    }

    synchronized public long decrement(long by) {
        this.lastUpdateTime = System.nanoTime();
        return count.addAndGet(-by);
    }

    public long count() {
        return count.longValue();
    }

    // In nano Second
    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    synchronized public Counter reset() {
        count.set(0);
        return this;
    }

    synchronized long operationsDone() {
        return this.count.longValue() - initialCount;
    }

    @Override
    public String toString() {
        return "Function Counter Name : "+this.counterName+" counter : "+this.count.longValue();
    }

    public long getInitialCount() {
        return initialCount;
    }
}
