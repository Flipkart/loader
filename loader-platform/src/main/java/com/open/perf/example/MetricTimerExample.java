package com.open.perf.example;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/2/13
 * Time: 6:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetricTimerExample extends Thread{

    private Timer[] timers;
    private int howMany;

    public MetricTimerExample(Timer[] timers, int howMany) {
        this.timers = timers;
        this.howMany = howMany;
    }

    public void run() {
        for(Timer timer : timers) {
            for(int i=0;i<this.howMany;i++) {
                TimerContext tc = timer.time();
                tc.stop();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        int howManyTimers = 1;
        Timer[] timers = new Timer[howManyTimers];
        for(int t=0; t< howManyTimers; t++) {
            timers[t] = Metrics.newTimer(new MetricName("G1", "T1", "N"+t), TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
        }

        int threads = 1;
        int rounds = 1000000;
        List<MetricTimerExample> timerThreads = new ArrayList<MetricTimerExample>();
        long startTime = System.nanoTime();
        for(int i=0; i<threads; i++) {
            MetricTimerExample me = new MetricTimerExample(timers, rounds/threads);
            me.start();
            timerThreads.add(me);
        }

        for(MetricTimerExample me : timerThreads) {
            me.join();
        }
        double timeTaken = (System.nanoTime() - startTime) / 1000d;
        long timeInstances = (rounds * howManyTimers);
        System.out.println("Time Taken : " + timeTaken + "");
        System.out.println("Total Time Counts : " + timeInstances + "");
        System.out.println("Time Spent per Time Count: "+(timeInstances / timeTaken) + "");

//        Snapshot snapshot = timer.getSnapshot();
//        System.out.println("Mean : "+(snapshot.getMedian()) + "");
//        System.out.println("99th : "+(snapshot.get99thPercentile()) + "");
//        System.out.println("75th : " + (snapshot.get75thPercentile()) + "");
//        snapshot.dump(new File("/tmp/yammer.txt"));

        /*
        1 thread
        Time Taken : 503
        Mean : 1974293.9036568266
        Min : 0.251
        Max : 1755.225
         */

        /*
        5 threads
        Time Taken : 517
        Mean : 1919374.1218875388
        Min : 0.251
        Max : 1797.711
         */
    }
}
