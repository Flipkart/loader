package com.open.perf.example;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.stats.Snapshot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/2/13
 * Time: 6:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetricTimer2Example extends Thread{
    private static Integer counter = new Integer(0);
    private final Timer timer;
    private final Meter meter;
    private final int howMany;
    private Random r = new Random();
    public MetricTimer2Example(Timer timer, Meter meter, int howMany) {
        this.timer = timer;
        this.howMany = howMany;
        this.meter = meter;
    }

    public void run() {
        for(int i=0;i<this.howMany;i++) {
            try {
                TimerContext tc = this.timer.time();
                Thread.sleep(r.nextInt(5));
                tc.stop();
                meter.mark();
            } catch (InterruptedException e){
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Timer timer = Metrics.newTimer(new MetricName("G1", "T1", "T1"), TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
        Meter meter = Metrics.newMeter(new MetricName("G1", "T1", "M1"), "", TimeUnit.SECONDS) ;
        int threadCount = 1000;
        int instances = 1000000;
        List<MetricTimer2Example> threads = new ArrayList<MetricTimer2Example>();
        for(int i=0;i<threadCount;i++) {
            MetricTimer2Example t = new MetricTimer2Example(timer, meter, instances);
            t.start();
            threads.add(t);
        }

        PrintThread pt = new PrintThread(timer, meter);
        pt.start();
        for(MetricTimer2Example t : threads) {
            t.join();
        }

        pt.stop();
        pt.print();
    }
}

class PrintThread extends Thread {
    private final Timer timer;
    private int dumpCounter = 0;
    private final Meter meter;

    public PrintThread(Timer t, Meter m) {
        this.timer = t;
        this.meter = m;
    }

    public void run() {
        while(true) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            print();
            dumpCounter++;
        }
    }

    public void print() {
        synchronized (timer) {
            long startTime = System.currentTimeMillis();
            Snapshot s = timer.getSnapshot();
            System.out.println(s.getValues().length);

            System.out.println(new Date().toString() + " Min :"+timer.min()
                    + ", Mean:" + timer.mean()
                    + ", 1M Rate:" + timer.oneMinuteRate()
                    + ", 5M Rate:" +timer.fiveMinuteRate()
                    + ", 15M Rate:" + timer.fifteenMinuteRate()
                    + ", Mean Rate:" + timer.meanRate()
                    + ", Max : "+timer.max()
                    + ", SD:"+timer.stdDev()
                    + ", 95th:"+s.get95thPercentile()
                    + ", 98th:" + s.get98thPercentile()
                    + ", 99th:" + s.get99thPercentile()
                    + ", 999th:"+s.get999thPercentile());
            System.out.println(new Date().toString() + " , 1M Rate:" + meter.oneMinuteRate()
                    + ", 5M Rate:" +meter.fiveMinuteRate()
                    + ", 15M Rate:" + meter.fifteenMinuteRate()
                    + ", Mean Rate:" + meter.meanRate()
                    + ", Count : "+meter.count());

            try {
                s.dump(new File("/tmp/snapshot"+ dumpCounter+".txt"));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            System.out.println("Dump Time :"+(System.currentTimeMillis() - startTime));
        }
    }
}