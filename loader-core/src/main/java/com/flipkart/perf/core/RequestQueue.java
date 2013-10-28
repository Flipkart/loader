package com.flipkart.perf.core;

import com.flipkart.perf.common.util.Clock;
import com.flipkart.perf.common.util.Counter;

/**
 * It appears to behave like a request queue from out side.
 * Inherently its a shared counter with all SequentialFunctionExecutors.
 */
public class RequestQueue {
    private Counter counter;
    private long endTimeMS = 0;
    private long howManyRequests = Long.MAX_VALUE;

    public RequestQueue(String groupName) {
        this(groupName, Long.MAX_VALUE);
    }

    public RequestQueue(String groupName, long howManyRequests) {
        this.howManyRequests = howManyRequests;
        this.counter = new Counter(groupName, "requestQueue", howManyRequests);
    }

    public RequestQueue setRequests(long howManyRequests) {
        this.howManyRequests = howManyRequests;
        this.counter = new Counter(this.counter.getGroupName(), this.counter.getCounterName(), howManyRequests);
        return this;
    }

    public RequestQueue setEndTimeMS(long futureTimeMS) {
        this.endTimeMS = futureTimeMS;
        return this;
    }

    public long getEndTimeMS() {
        return endTimeMS;
    }

    public long getHowManyRequests() {
        return howManyRequests;
    }

    synchronized public boolean hasRequest() {
        return (this.counter.decrement() >= 0)
                && (this.endTimeMS <= 0 || this.endTimeMS > Clock.milliTick());
    }

    synchronized public long requestsPending() {
        return counter.count();
    }
}
