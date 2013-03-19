package com.open.perf.core;

import com.open.perf.util.Clock;
import com.open.perf.util.Counter;
import com.open.perf.util.FileHelper;
import com.open.perf.util.Timer;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class StatsCollectorThread extends Thread{
    private static final int STATS_QUEUE_POLL_INTERVAL = 1000; // ms
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
    private final String statsBasePath;
    private final Map<String,FunctionCounter> functionCounters;
    private final List<String> customTimers;
    private final Map<String, Counter> customCounters;
    private final long startTime;
    private final HashMap<String, BufferedWriter> filePathWriterMap;

    public StatsCollectorThread(String statsBasePath,
                                GroupStatsQueue groupStatsQueue,
                                Map<String, FunctionCounter> functionCounters,
                                List<String> customTimerNames,
                                Map<String, Counter> customCounters,
                                long startTime) throws FileNotFoundException {
        this.statsBasePath = statsBasePath;
        this.groupStatsQueue = groupStatsQueue;
        this.lastQueueSwapTime = System.currentTimeMillis();
        this.fileWriters = new LinkedHashMap<String, BufferedWriter>();
        this.allCounters = new ArrayList<Counter>();
        this.functionCounters = functionCounters;
        this.customTimers = customTimerNames;
        this.customCounters = customCounters;
        this.startTime = startTime;
        for(String counter : customCounters.keySet()) {
            Counter customCounter = customCounters.get(counter);
            this.allCounters.add(customCounter);
        }

        for(String function : functionCounters.keySet()) {
            FunctionCounter functionCounter = functionCounters.get(function);
            this.allCounters.add(functionCounter.getCount());
            this.allCounters.add(functionCounter.getErrorCounter());
            this.allCounters.add(functionCounter.getFailureCounter());
            this.allCounters.add(functionCounter.getSkipCounter());
        }

        this.filePathWriterMap = new HashMap<String, BufferedWriter>();

    }

    private void createFileWriters(int filePartId) throws FileNotFoundException {
        for(String timer : customTimers) {
            String filePath = statsBasePath + File.separator + "timers" + File.separator + timer + ".part"+filePartId;
            FileHelper.createFilePath(filePath);
            this.fileWriters.put(timer, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));
            this.filePathWriterMap.put(filePath, this.fileWriters.get(timer));
        }

        for(String counter : customCounters.keySet()) {
            String filePath = statsBasePath + File.separator + "counters" + File.separator + counter + ".part"+filePartId;
            FileHelper.createFilePath(filePath);

            this.fileWriters.put(customCounters.get(counter).getCounterName(),
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));
            this.filePathWriterMap.put(filePath, this.fileWriters.get(customCounters.get(counter).getCounterName()));
        }

        for(String function : functionCounters.keySet()) {
            // For execution Times

            String filePath = statsBasePath + File.separator + "timers" + File.separator + function + ".part"+filePartId;
            FileHelper.createFilePath(filePath);

            this.fileWriters.put(function, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));
            this.filePathWriterMap.put(filePath, this.fileWriters.get(function));

            // For all counters
            FunctionCounter functionCounter = functionCounters.get(function);

            filePath = statsBasePath + File.separator + "counters" + File.separator + functionCounter.getCount().getCounterName() + ".part"+filePartId;
            FileHelper.createFilePath(filePath);
            this.fileWriters.put(functionCounter.getCount().getCounterName(),
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));
            this.filePathWriterMap.put(filePath, this.fileWriters.get(functionCounter.getCount().getCounterName()));

            filePath = statsBasePath + File.separator + "counters" + File.separator + functionCounter.getErrorCounter().getCounterName() + ".part"+filePartId;
            FileHelper.createFilePath(filePath);
            this.fileWriters.put(functionCounter.getErrorCounter().getCounterName(),
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));
            this.filePathWriterMap.put(filePath, this.fileWriters.get(functionCounter.getErrorCounter().getCounterName()));

            filePath = statsBasePath + File.separator + "counters" + File.separator + functionCounter.getFailureCounter().getCounterName() + ".part"+filePartId;
            FileHelper.createFilePath(filePath);
            this.fileWriters.put(functionCounter.getFailureCounter().getCounterName(),
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));
            this.filePathWriterMap.put(filePath, this.fileWriters.get(functionCounter.getFailureCounter().getCounterName()));

            filePath = statsBasePath + File.separator + "counters" + File.separator + functionCounter.getSkipCounter().getCounterName() + ".part"+filePartId;
            FileHelper.createFilePath(filePath);
            this.fileWriters.put(functionCounter.getSkipCounter().getCounterName(),
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath))));
            this.filePathWriterMap.put(filePath, this.fileWriters.get(functionCounter.getSkipCounter().getCounterName()));
        }
        if(filePartId == 0) {
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
    }

    public void run() {
        granularDelay();
        int collectionCount = 0;
        while(this.keepRunning) {
            collectStats(collectionCount);
            granularDelay();
            collectionCount++;
        }
        waitForCollectionToGetOver();
        swapQueues();
        collectStats(collectionCount++);
        swapQueues();
        collectStats(collectionCount++);
        closeFiles();
    }

    private void completeFileWriters() throws IOException {
        for(String filePath : this.filePathWriterMap.keySet()) {
            BufferedWriter bw = this.filePathWriterMap.get(filePath);
            bw.close();
            new File(filePath).renameTo(new File(filePath+".done"));
        }
        this.filePathWriterMap.clear();
        this.fileWriters.clear();
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

    private void collectStats(int collectionCount){
        long startTime = System.currentTimeMillis();
        try {
            createFileWriters(collectionCount);
            this.collectingStats = true;
            GroupStatsInstance groupStatsInstance = null;
            while((groupStatsInstance = this.groupStatsQueue.getGroupStats()) != null) {
                dumpTimers(groupStatsInstance.getCustomTimers());
                dumpTimers(groupStatsInstance.getFunctionTimers());
            }
            dumpCounters();
            this.collectingStats = false;
            completeFileWriters();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException(e);
        }

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
                counter.reset();
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
                System.out.println(bufferedData);
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
