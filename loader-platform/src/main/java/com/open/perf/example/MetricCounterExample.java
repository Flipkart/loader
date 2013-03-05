package com.open.perf.example;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
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
public class MetricCounterExample extends Thread{

    private Counter[] counters;
    private int howMany;

    public MetricCounterExample(Counter[] counters, int howMany) {
        this.counters = counters;
        this.howMany = howMany;
    }

    public void run() {
        for(Counter counter : counters) {
            for(int i=0;i<this.howMany;i++) {
                counter.inc();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        int howManyCounters = 1;
        Counter[] counters = new Counter[howManyCounters];
        for(int t=0; t< howManyCounters; t++) {
            counters[t] = Metrics.newCounter(new MetricName("G1", "T1", "N"+t));
        }

        int threads = 1;
        int rounds = 1000000;
        List<MetricCounterExample> counterThreads = new ArrayList<MetricCounterExample>();
        long startTime = System.nanoTime();
        for(int i=0; i<threads; i++) {
            MetricCounterExample me = new MetricCounterExample(counters, rounds/threads);
            me.start();
            counterThreads.add(me);
        }

        for(MetricCounterExample me : counterThreads) {
            me.join();
        }
        double timeTaken = (System.nanoTime() - startTime) / 1000d;
        long timeInstances = (rounds * howManyCounters);
        System.out.println("Time Taken : " + timeTaken + "");
        System.out.println("Total Time Counts : " + timeInstances + "");
        System.out.println("Time Spent per Time Count: "+(timeInstances / timeTaken) + "");

//        Snapshot snapshot = counterp.getSnapshot();
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
