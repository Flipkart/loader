package com.open.perf.core;

import com.open.perf.util.Clock;
import com.open.perf.util.Counter;

/**
 * It appears to behave like a request queue from out side.
 * Inherently its a shared counter with all SequentialFunctionExecutors.
 */
public class RequestQueue {
    private Counter counter;
    private long endTimeMS = -1;

    public RequestQueue(String groupName) {
        this(groupName, Long.MAX_VALUE);
    }

    public RequestQueue(String groupName, long requests) {
        this.counter = new Counter(groupName, "requestQueue", requests);
    }

    public RequestQueue(String groupName, String name, long operationsNeedToBeDone, long endTimeMS) {
        this.counter = new Counter(groupName, name, operationsNeedToBeDone);
        this.endTimeMS = endTimeMS;
    }

    public RequestQueue setRequests(long requests) {
        this.counter = new Counter(this.counter.getGroupName(), this.counter.getCounterName(), requests);
        return this;
    }

    public RequestQueue setEndTimeMS(long futureTimeMS) {
        this.endTimeMS = futureTimeMS;
        return this;
    }

    synchronized public boolean getRequest() {
        return (this.counter.decrement() >= 0)
                && (this.endTimeMS == -1 || this.endTimeMS > Clock.milliTick());
    }

    synchronized public long requestsPending() {
        return counter.count();
    }
}
