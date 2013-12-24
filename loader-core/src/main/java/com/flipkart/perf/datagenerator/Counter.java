package com.flipkart.perf.datagenerator;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 23/10/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Counter extends DataGenerator{
    private long startValue;
    private long currentValue;
    private final int jump;
    private final long maxValue;

    public Counter(long startValue, int jump, long maxValue) {
        this.startValue = startValue;
        this.currentValue = startValue;
        this.jump = jump;
        this.maxValue = maxValue;
    }

    synchronized public String next() {
        String valueToReturn = String.valueOf(currentValue);
        currentValue += jump;
        if(currentValue > maxValue)
            currentValue = startValue;
        return valueToReturn;
    }
}
