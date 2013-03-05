package com.open.perf.util;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 4/3/13
 * Time: 3:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class Clock {
    public static long tick() {
        return System.nanoTime();
    }
}
