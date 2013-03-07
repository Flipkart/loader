package com.open.perf.util;

/**
 * Time Helper
 */
public class Clock {

    public static long nsTick() {
        return System.nanoTime();
    }

    public static long microTick() {
        return System.nanoTime() / 1000;
    }

    public static long milliTick() {
        return System.currentTimeMillis();
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
