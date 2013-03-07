package com.open.perf.core;

import com.open.perf.util.Clock;
import com.open.perf.util.Counter;
import com.open.perf.util.FileHelper;
import com.open.perf.util.Timer;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatsCollectorThread extends Thread{
    private static final int STATS_QUEUE_POLL_INTERVAL = 2000; // ms
    private static final int SWAP_QUEUE_INTERVAL = 5000; // ms
    private static final int BULK_WRITE_SIZE = 100;

    private boolean keepRunning = true;
    private final GroupStatsQueue groupStatsQueue;
    private Map<String,BufferedWriter> fileWriters;

    private static Logger logger;
    static {
        logger      = Logger.getLogger(StatsCollectorThread.class);
    }

    private long lastQueueSwapTime;
    private boolean collectingStats;
    private List<Counter> allCounters;
    public StatsCollectorThread(String statsBasePath,
                                GroupStatsQueue groupStatsQueue,
                                Map<String, FunctionCounter> functionCounters,
                                List<String> customTimerNames,
                                Map<String, Counter> customCounters,
                                long startTime) throws FileNotFoundException {

        this.groupStatsQueue = groupStatsQueue;
        this.lastQueueSwapTime = System.currentTimeMillis();
        this.fileWriters = new LinkedHashMap<String, BufferedWriter>();
        this.allCounters = new ArrayList<Counter>();

        for(String timer : customTimerNames) {
            String filePath = statsBasePath + File.separator + "timers" + File.separator + timer;
            FileHelper.createFilePath(filePath);
            this.fileWriters.put(timer, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));
        }

        for(String counter : customCounters.keySet()) {
            Counter customCounter = customCounters.get(counter);
            this.allCounters.add(customCounter);

            String filePath = statsBasePath + File.separator + "counters" + File.separator + counter;
            FileHelper.createFilePath(filePath);

            this.fileWriters.put(customCounters.get(counter).getCounterName(),
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));
        }

        for(String function : functionCounters.keySet()) {
            // For execution Times

            String filePath = statsBasePath + File.separator + "timers" + File.separator + function;
            FileHelper.createFilePath(filePath);

            this.fileWriters.put(function, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));

            // For all counters
            FunctionCounter functionCounter = functionCounters.get(function);

            this.allCounters.add(functionCounter.getCount());
            this.allCounters.add(functionCounter.getErrorCounter());
            this.allCounters.add(functionCounter.getFailureCounter());
            this.allCounters.add(functionCounter.getSkipCounter());

            filePath = statsBasePath + File.separator + "counters" + File.separator + functionCounter.getCount().getCounterName();
            FileHelper.createFilePath(filePath);
            this.fileWriters.put(functionCounter.getCount().getCounterName(),
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));

            filePath = statsBasePath + File.separator + "counters" + File.separator + functionCounter.getErrorCounter().getCounterName();
            FileHelper.createFilePath(filePath);
            this.fileWriters.put(functionCounter.getErrorCounter().getCounterName(),
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));

            filePath = statsBasePath + File.separator + "counters" + File.separator + functionCounter.getFailureCounter().getCounterName();
            FileHelper.createFilePath(filePath);
            this.fileWriters.put(functionCounter.getFailureCounter().getCounterName(),
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));

            filePath = statsBasePath + File.separator + "counters" + File.separator + functionCounter.getSkipCounter().getCounterName();
            FileHelper.createFilePath(filePath);
            this.fileWriters.put(functionCounter.getSkipCounter().getCounterName(),
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));
        }

        // initialize Counter file
        for(Counter counter : this.allCounters) {
            BufferedWriter bw = this.fileWriters.get(counter.getCounterName());
            try {
                bw.write(startTime + ",0\n");
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            }
        }
    }

    public void run() {
        granularDelay();
        while(this.keepRunning) {
            collectStats();
            granularDelay();
        }
        waitForCollectionToGetOver();
        swapQueues();
        collectStats();
        swapQueues();
        collectStats();
        closeFiles();
    }

    private void waitForCollectionToGetOver() {
        while(this.collectingStats) {
            Clock.sleep(200);
        }
    }

    private void swapQueues() {
        groupStatsQueue.swapQueues();
    }

    private void closeFiles() {
        for(BufferedWriter bw : this.fileWriters.values())
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            }
    }

    private void granularDelay() {
        int totalDelay = 0;
        int granularDelay = 200;
        while(totalDelay < this.STATS_QUEUE_POLL_INTERVAL && this.keepRunning) {
            try {
                Thread.sleep(granularDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            }
            totalDelay += granularDelay;

            if(System.currentTimeMillis() - this.lastQueueSwapTime > this.SWAP_QUEUE_INTERVAL && canSwapQueues()) {
                swapQueues();
                this.lastQueueSwapTime = System.currentTimeMillis();
            }
        }
    }

    private void collectStats() {
        long startTime = System.currentTimeMillis();

        this.collectingStats = true;
        GroupStatsInstance groupStatsInstance = null;
        while((groupStatsInstance = this.groupStatsQueue.getGroupStats()) != null) {
            dumpTimers(groupStatsInstance.getCustomTimers());
            dumpTimers(groupStatsInstance.getFunctionTimers());
        }
        dumpCounters();
        this.collectingStats = false;

        logger.debug("Time To Print Stats :" + (System.currentTimeMillis() - startTime));
    }


    private boolean canSwapQueues() {
        return !this.collectingStats;
    }

    private void dumpCounters(){
        for(Counter counter: this.allCounters) {
            BufferedWriter bw = this.fileWriters.get(counter.getCounterName());
            synchronized (counter) {
                writeToFile(bw, counter.getLastUpdateTime() + "," + counter.count() + "\n");
            }
        }
    }

    private void dumpTimers(Map<String, Timer> timers) {
        for(String timer : timers.keySet()) {
            int buffered = 0 ;
            String bufferedData = "";
            BufferedWriter bw = this.fileWriters.get(timer);
            Map<Long, Double> timeList = timers.get(timer).getTimeList();
            for(Long timerStamp : timeList.keySet()) {
                bufferedData += timerStamp.toString() + "," + timeList.get(timerStamp).toString() + "\n";
                buffered++;
                if(buffered % this.BULK_WRITE_SIZE == 0) {
                    writeToFile(bw, bufferedData);
                    bufferedData = "";
                    buffered = 0;
                }
            }
            if(!bufferedData.equals("")) {
                writeToFile(bw, bufferedData);
            }

        }
    }

    private void writeToFile(BufferedWriter bw, String content) {
        try {
            bw.write(content);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException(e);
        }
    }

    public void stopIt() {
        this.keepRunning = false;
    }
}
