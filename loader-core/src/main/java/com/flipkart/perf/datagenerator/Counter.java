package com.flipkart.perf.datagenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 23/10/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Counter extends DataGenerator{
    private long currentValue;
    private final int jump;

    public Counter(long startValue, int jump) {
        this.currentValue = startValue;
        this.jump = jump;
    }

    synchronized public String next() {
        String valueToReturn = String.valueOf(currentValue);
        currentValue += jump;
        return valueToReturn;
    }
}
