package com.open.perf.util;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 3/3/13
 * Time: 1:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimerInstance {
    private Timer timer;
    private long startTime;

    public TimerInstance(Timer timer) {
        this.timer = timer;
        this.startTime = Clock.tick();
    }

    public long stop() {
        long timeInNS = Clock.tick() - this.startTime;
        this.timer.add(timeInNS);
        return timeInNS;
    }
}
