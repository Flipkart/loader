package com.open.perf.core;

import com.open.perf.util.Counter;

/**
 * It appears to behave like a request queue from out side.
 * Inherently its a shared counter with all SequentialFunctionExecutors.
 */
public class RequestQueue {
    private Counter counter;

    public RequestQueue(String groupName, String name) {
        this(groupName, name, Long.MAX_VALUE);
    }

    public RequestQueue(String groupName, String name, long operationsNeedToBeDone) {
        this.counter = new Counter(groupName, name, operationsNeedToBeDone);
    }

    synchronized public boolean getRequest() {
        return counter.decrement() >= 0;
    }

    synchronized public long requestsPending() {
        return counter.count();
    }
}
