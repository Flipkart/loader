package com.flipkart.perf.datagenerator;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 23/10/13
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class FixedValue extends DataGenerator{

    private final String value;

    public FixedValue(String value) {
        this.value = value;
    }

    @Override
    public String next() {
        return value;
    }
}
