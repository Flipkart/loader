package com.flipkart.perf.datagenerator;


import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 */
public class UseAndRemove extends DataGenerator{
    private final LinkedList<Object> list;

    public UseAndRemove(LinkedList<Object> list) {
        this.list = list;
    }

    @Override
    public String next() {
        Object value = list.pollFirst();
        return value == null ? null : value.toString();
    }
}
