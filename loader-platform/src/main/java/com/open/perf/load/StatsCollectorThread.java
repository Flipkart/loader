package com.open.perf.load;

import com.open.perf.util.Counter;
import com.open.perf.util.HelperUtil;
import com.open.perf.util.Timer;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 20/2/13
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class StatsCollectorThread extends Thread{
    private final int statsQueuePollInterval;
    private boolean keepRunning = true;
    private final GroupStatsQueue groupStatsQueue;
    private Map<String,BufferedWriter> fileWriters;
    private int bulkWriteSize = 100;

    private static Logger logger;
    static {
        logger      = Logger.getLogger(StatsCollectorThread.class);
    }

    private GroupControllerNew groupController;
    private final int swapQueueInterval;
    private long lastQueueSwapTime;
    private boolean collectingStats;

    public StatsCollectorThread(GroupStatsQueue groupStatsQueue,
                                Map<String, FunctionCounter> functionCounters,
                                List<String> customTimerNames,
                                Map<String, Counter> customCounters) throws FileNotFoundException {

        this.statsQueuePollInterval = 1000;
        this.groupStatsQueue = groupStatsQueue;
        this.swapQueueInterval = 5000;
        this.lastQueueSwapTime = System.currentTimeMillis();
        this.fileWriters = new LinkedHashMap<String, BufferedWriter>();

        for(String timer : customTimerNames) {
            this.fileWriters.put(timer, new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/tmp/"+timer))));
        }

        for(String counter : customCounters.keySet()) {
            this.fileWriters.put(counter, new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/tmp/"+counter))));
        }

        for(String function : functionCounters.keySet()) {
            FunctionCounter functionCounter = functionCounters.get(function);
            this.fileWriters.put(function, new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/tmp/"+functionCounter.getCount().getCounterName()))));
            this.fileWriters.put(function, new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/tmp/"+functionCounter.getErrorCounter().getCounterName()))));
            this.fileWriters.put(function, new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/tmp/"+functionCounter.getFailureCounter().getCounterName()))));
            this.fileWriters.put(function, new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/tmp/"+functionCounter.getSkipCounter().getCounterName()))));
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
            HelperUtil.delay(200);
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
        while(totalDelay < this.statsQueuePollInterval && this.keepRunning) {
            try {
                Thread.sleep(granularDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            }
            totalDelay += granularDelay;

            if(System.currentTimeMillis() - this.lastQueueSwapTime > this.swapQueueInterval && canSwapQueues()) {
                swapQueues();
                this.lastQueueSwapTime = System.currentTimeMillis();
            }
        }
    }

    private boolean canSwapQueues() {
        return !this.collectingStats;
    }

    private void collectStats() {
        long startTime = System.currentTimeMillis();
        synchronized (this.groupStatsQueue) {
            this.collectingStats = true;
            logger.info("COLLECTING STATS. NO LOAD RIGHT NOW");
            // Collect Data. Try to avoid synchronization . Just get snapshot of data
            GroupStatsInstance groupStatsInstance = null;
            while((groupStatsInstance = this.groupStatsQueue.getGroupStats()) != null) {
                try {
                    dumpTimers(groupStatsInstance.getCustomTimers());
                    dumpFunctionExecutionTime(groupStatsInstance.getFunctionTimers());
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    throw new RuntimeException(e);
                }

/*
                readFunctionExecutions(groupStatsInstance.getFunctionExecutionCount());
                readFunctionFailures(groupStatsInstance.getFunctionFailureCount());
                readFunctionErrors(groupStatsInstance.getFunctionErrorCount());
                readFunctionSkips(groupStatsInstance.getFunctionSkipCount());
*/
            }
            logger.info("STATS COLLECTION DONE.");
/*
            logger.info("Functions Stats :"+functionStatsMap);
            logger.info("Counters :"+counters);
            logger.info("Timers :"+timers);
*/
            this.collectingStats = false;
        }

        System.out.println("Time To Print Stats :" + (System.currentTimeMillis() - startTime));
        // Crunch the Data
    }

    private void dumpFunctionExecutions(Map<String, Integer> functionExecutionCount) {
        //To change body of created methods use File | Settings | File Templates.
    }

    /*
        private void readFunctionSkips(Map<String, Integer> functionSkipCount) {
            for(String function : functionSkipCount.keySet()) {
                this.functionStatsMap.get(function).skipped();
            }
        }

        private void readFunctionErrors(Map<String, Integer> functionErrorCount) {
            for(String function : functionErrorCount.keySet()) {
                this.functionStatsMap.get(function).errored();
            }
        }

        private void readFunctionFailures(Map<String, Integer> functionFailureCount) {
            for(String function : functionFailureCount.keySet()) {
                this.functionStatsMap.get(function).failed();
            }
        }

        private void readFunctionExecutions(Map<String, Integer> functionExecutionCount) {
            for(String function : functionExecutionCount.keySet()) {
                this.functionStatsMap.get(function).executed(functionExecutionCount.get(function));
            }
        }

        private void readTimers(Map<String, Timer> timers) {
            for(String timer : timers.keySet()) {
                this.timers.get(timer).add(timers.get(timer).getTimeList());
            }
        }

        private void readCounters(Map<String, Counter> counters) {
            for(String counter : counters.keySet())
                this.counters.get(counter).increment(counters.get(counter).count());
        }
    */
    private void dumpCounters(Map<String, Counter> counters) throws IOException {
        for(String counter : counters.keySet()) {
            this.fileWriters.get(counter).write(String.valueOf(counters.get(counter).count())+"\n");
            this.fileWriters.get(counter).flush();
        }
    }

    private void dumpTimers(Map<String, Timer> timers) throws IOException {
        for(String timer : timers.keySet()) {
            int buffered = 0 ;
            String bufferedData = "";
            BufferedWriter bw = this.fileWriters.get(timer);
            Map<Long, Double> timeList = timers.get(timer).getTimeList();
            for(Long timerStamp : timeList.keySet()) {
                bufferedData += timerStamp.toString() + "," + timeList.get(timerStamp).toString() + "\n";
                buffered++;
                if(buffered % this.bulkWriteSize == 0) {
                    bw.write(bufferedData);
                    bw.flush();
                    bufferedData = "";
                    buffered = 0;
                }
            }
            if(!bufferedData.equals("")) {
                bw.write(bufferedData);
                bw.flush();
            }

        }
    }

    private void dumpFunctionExecutionTime(Map<String, Timer> functionExecutionTimeMap) throws IOException {
        for(String function : functionExecutionTimeMap.keySet()) {
            int buffered = 0 ;
            String bufferedData = "";
            BufferedWriter bw = this.fileWriters.get(function);
            Map<Long, Double> timeList = functionExecutionTimeMap.get(function).getTimeList();
            for(Long timerStamp : timeList.keySet()) {
                bufferedData += timerStamp.toString() + ","+timeList.get(timerStamp).toString() + "\n";
                buffered++;
                if(buffered % this.bulkWriteSize == 0) {
                    bw.write(bufferedData);
                    bw.flush();
                    bufferedData = "";
                    buffered = 0;
                }
            }
            if(!bufferedData.equals("")) {
                bw.write(bufferedData);
                bw.flush();
            }
        }
    }

    public void setGroupController(GroupControllerNew groupController) {
        this.groupController = groupController;
    }

    public void stopIt() {
        this.keepRunning = false;
    }
}
