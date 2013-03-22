package com.open.perf.util;

import com.open.perf.constant.MathConstant;

import java.util.Date;

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

    public static void sleepSec(int secs) {
        sleep(secs * 1000);
    }
    public static void sleepMin(int mins) {
        sleep(mins * 60 * 1000);
    }

    public static Date dateFromNS(long oldNS) {
        long currentNS = nsTick();
        long nsPassed = currentNS - oldNS;
        long msPassed = nsPassed / MathConstant.MILLION;
        long oldMS = Clock.milliTick() - msPassed;
        return new Date(oldMS);
    }

    public static Date dateFromMS(long ms) {
        return new Date(ms);
    }

    public static void main(String[] args) {
        System.out.println(dateFromNS(System.nanoTime()).toString());
    }
}
