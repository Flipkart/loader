package com.flipkart.perf.util;

import com.flipkart.perf.common.util.Clock;

import java.util.concurrent.atomic.AtomicLong;

/**
 * nitinka
 */
public class Counter {
    private final String groupName;
    private final String functionName;
    private final String counterName;

    private long initialCount;
    private AtomicLong count;
    private long lastUpdateTimeMS;

    public Counter(String groupName, String functionName, String counterName) {
        this(groupName,functionName, counterName, 0);
        this.lastUpdateTimeMS = Clock.milliTick();
    }

    public Counter(String groupName, String functionName, String counterName, long initialCount) {
        this.groupName = groupName;
        this.functionName = functionName;
        this.counterName = counterName;
        count = new AtomicLong(initialCount);
        this.initialCount = initialCount;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getCounterName() {
        return counterName;
    }

    public long increment() {
        synchronized (this) {
            this.lastUpdateTimeMS = Clock.milliTick();
            return count.incrementAndGet();
        }
    }

    public long increment(long by) {
        synchronized (this) {
            this.lastUpdateTimeMS = Clock.milliTick();
            return count.addAndGet(by);
        }
    }

    public long decrement() {
        synchronized (this) {
            this.lastUpdateTimeMS = Clock.milliTick();
            return count.decrementAndGet();
        }
    }

    public long decrement(long by) {
        synchronized (this) {
            this.lastUpdateTimeMS = Clock.milliTick();
            return count.addAndGet(-by);
        }
    }

    public long count() {
        return count.longValue();
    }

    // In nano Second
    public long getLastUpdateTimeMS() {
        return this.lastUpdateTimeMS;
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