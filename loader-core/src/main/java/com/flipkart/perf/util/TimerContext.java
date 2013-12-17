package com.flipkart.perf.util;

import com.flipkart.perf.common.util.Clock;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 3/3/13
 * Time: 1:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimerContext {
    private Timer timer;
    private long startTime;

    public TimerContext(Timer timer) {
        this.timer = timer;
        this.startTime = Clock.nsTick();
    }

    public long stop() {
        if(timer == null)
            return -1;
        long timeInNS = Clock.nsTick() - this.startTime;
        this.timer.add(timeInNS);
        return timeInNS;
    }
}
