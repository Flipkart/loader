package com.open.perf.load;

import com.open.perf.util.Counter;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 19/2/13
 * Time: 1:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class RequestQueue {
    private Counter counter;

    public RequestQueue(String name) {
        this(name, Long.MAX_VALUE);
    }

    public RequestQueue(String name, long operationsNeedToBeDone) {
        this.counter = new Counter(name, operationsNeedToBeDone);
    }

    synchronized public boolean getRequest() {
        return counter.decrement() >= 0;
    }

    synchronized public long requestsPending() {
        return counter.count();
    }
}
