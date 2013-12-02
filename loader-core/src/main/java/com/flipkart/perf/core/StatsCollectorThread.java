package com.flipkart.perf.core;

import com.flipkart.perf.common.util.Clock;
import com.flipkart.perf.util.Histogram;
import com.flipkart.perf.domain.GroupFunction;
import com.flipkart.perf.util.Counter;
import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.common.util.TimeInstance;
import com.flipkart.perf.domain.Group;
import com.flipkart.perf.util.HistogramInstance;
import com.flipkart.perf.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class StatsCollectorThread extends Thread{
    private static final int STATS_QUEUE_POLL_INTERVAL = 5000; // ms
    private static final int SWAP_QUEUE_INTERVAL = 5000; // ms
    private static final int BULK_WRITE_SIZE = 100;

    private boolean keepRunning = true;
    private final GroupStatsQueue groupStatsQueue;
    private Map<String,BufferedWriter> statsFileWritersMap;

    private static Logger logger = LoggerFactory.getLogger(StatsCollectorThread.class);

    private long lastQueueSwapTime;
    private boolean collectingStats;
    private List<Counter> allCounters;
    private final String statsBasePath;
    private final long startTimeMS;
    private final Map<String,BufferedWriter> fileWriterMap;
    private final Group group;

    public StatsCollectorThread(String statsBasePath,
                                GroupStatsQueue groupStatsQueue,
                                Map<String, FunctionCounter> functionCounters,
                                Group group,
                                Map<String, Counter> customCounters,
                                long startTimeMS) throws FileNotFoundException {
        this.statsBasePath = statsBasePath;
        this.groupStatsQueue = groupStatsQueue;
        this.lastQueueSwapTime = Clock.milliTick();
        this.statsFileWritersMap = new LinkedHashMap<String, BufferedWriter>();
        this.allCounters = new ArrayList<Counter>();
        this.startTimeMS = startTimeMS;
        this.group = group;

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
        this.fileWriterMap = new HashMap<String, BufferedWriter>();
    }

    private void createFileWriters(int filePartId) throws FileNotFoundException {
        // Creating File Writer for real time Group configuration
        String filePath = statsBasePath
                + File.separator + "conf"
                + File.separator + "realTimeConf"
                + ".part" + filePartId;

        FileHelper.createFilePath(filePath);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
        this.statsFileWritersMap.put("realTimeConf", bw);
        this.fileWriterMap.put(filePath, bw);

        for(GroupFunction groupFunction : group.getFunctions()) {
            if(groupFunction.isDumpData()) {

                // Creating File Writer for Function Execution Time
                filePath = statsBasePath
                        + File.separator + "functions" + File.separator + groupFunction.getFunctionalityName()
                        + File.separator + "timers" + File.separator + groupFunction.getFunctionalityName()
                        + ".part" + filePartId;

                FileHelper.createFilePath(filePath);
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
                this.statsFileWritersMap.put(groupFunction.getFunctionalityName() + "." + groupFunction.getFunctionalityName(), bw);
                this.fileWriterMap.put(filePath, bw);

                // Creating File Writers for Function Counters
                String[] functionCounters = new String[]{
                        groupFunction.getFunctionalityName() + "_count",
                        groupFunction.getFunctionalityName() + "_error",
                        groupFunction.getFunctionalityName() + "_failure",
                        groupFunction.getFunctionalityName() + "_skip"};

                for(String functionCounter : functionCounters) {
                    filePath = statsBasePath
                            + File.separator + "functions" + File.separator + groupFunction.getFunctionalityName()
                            + File.separator + "counters" + File.separator + functionCounter
                            + ".part" + filePartId;

                    FileHelper.createFilePath(filePath);
                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
                    this.statsFileWritersMap.put(groupFunction.getFunctionalityName() + "." + functionCounter, bw);
                    this.fileWriterMap.put(filePath, bw);
                }

                // Creating File Writer for Custom Timers
                for(String customTimerName : groupFunction.getCustomTimers()) {
                    filePath = statsBasePath
                            + File.separator + "functions" + File.separator + groupFunction.getFunctionalityName()
                            + File.separator +  "timers" + File.separator + customTimerName
                            + ".part" + filePartId;

                    FileHelper.createFilePath(filePath);
                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
                    this.statsFileWritersMap.put(groupFunction.getFunctionalityName() + "." + customTimerName, bw);
                    this.fileWriterMap.put(filePath, bw);
                }

                // Creating File Writer for Custom Histograms
                for(String customHistogramName : groupFunction.getCustomHistograms()) {
                    filePath = statsBasePath
                            + File.separator + "functions" + File.separator + groupFunction.getFunctionalityName()
                            + File.separator +  "histograms" + File.separator + customHistogramName
                            + ".part" + filePartId;

                    FileHelper.createFilePath(filePath);
                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
                    this.statsFileWritersMap.put(groupFunction.getFunctionalityName() + "." + customHistogramName, bw);
                    this.fileWriterMap.put(filePath, bw);
                }

                // Creating File Writer for Custom Counters
                for(String customCounterName : groupFunction.getCustomCounters()) {
                    filePath = statsBasePath
                            + File.separator + "functions" + File.separator + groupFunction.getFunctionalityName()
                            + File.separator +  "counters" + File.separator + customCounterName
                            + ".part" + filePartId;

                    FileHelper.createFilePath(filePath);
                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
                    this.statsFileWritersMap.put(groupFunction.getFunctionalityName() + "." + customCounterName, bw);
                    this.fileWriterMap.put(filePath, bw);
                }
            }
        }

        if(filePartId == 0) {
            // initialize Counter file
            for(Counter counter : this.allCounters) {
                bw = this.statsFileWritersMap.get(counter.getFunctionName() + "." + counter.getCounterName());
                if(bw != null) {
                    try {
                        bw.write(startTimeMS + ",0\n");
                        bw.flush();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public void run() {
        granularDelay();
        int collectionCount = 0;
        while(this.keepRunning) {
            collectStats(collectionCount++);
            granularDelay();
        }
        waitForCollectionToGetOver();
        collectStats(collectionCount++);
        swapQueues();
        collectStats(collectionCount++);
        closeFiles();
    }

    private void completeFileWriters() throws IOException {
        for(String filePath : this.fileWriterMap.keySet()) {
            BufferedWriter bw = this.fileWriterMap.get(filePath);
            bw.close();
            new File(filePath).renameTo(new File(filePath+".done"));
        }
        this.fileWriterMap.clear();
        this.statsFileWritersMap.clear();
    }

    private void waitForCollectionToGetOver() {
        while(this.collectingStats) {
            try {
                Clock.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void swapQueues() {
        groupStatsQueue.swapQueues();
    }

    private void closeFiles() {
        for(BufferedWriter bw : this.statsFileWritersMap.values())
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

            if(Clock.milliTick() - this.lastQueueSwapTime > this.SWAP_QUEUE_INTERVAL && canSwapQueues()) {
                swapQueues();
                this.lastQueueSwapTime = Clock.milliTick();
            }
        }
    }

    private void collectStats(int collectionCount){
        long collectionStartTime = Clock.milliTick();
        try {
            createFileWriters(collectionCount);
            this.collectingStats = true;
            GroupStatsInstance groupStatsInstance = null;
            while((groupStatsInstance = this.groupStatsQueue.getGroupStats()) != null) {
                dumpTimers(groupStatsInstance.getCustomTimers());
                dumpTimers(groupStatsInstance.getFunctionTimers());
                dumpHistograms(groupStatsInstance.getFunctionHistograms());
            }
            dumpCounters();
            dumpRealTimeGroupConf();
            this.collectingStats = false;
            completeFileWriters();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException(e);
        }

        logger.debug("Time To Print Stats :" + (Clock.milliTick() - collectionStartTime));
    }

    private boolean canSwapQueues() {
        return !this.collectingStats;
    }

    private void dumpRealTimeGroupConf() {
        BufferedWriter bw = this.statsFileWritersMap.get("realTimeConf");
        writeToFile(bw, Clock.milliTick()
                + "," + this.group.getThreads()
                + "," + this.group.getThroughput()
                + "\n");
    }

    private void dumpCounters(){
        for(Counter counter: this.allCounters) {
            BufferedWriter bw = this.statsFileWritersMap.get(counter.getFunctionName() + "." + counter.getCounterName());
            if(bw != null) {
                synchronized (counter) {
//                    if((Clock.milliTick() - counter.getLastUpdateTimeMS() < (4 * STATS_QUEUE_POLL_INTERVAL))) //  Dumb optimization to avoid writing if counter is not being updated in last 4 dump cycles
//                    writeToFile(bw, counter.getLastUpdateTimeMS() + "," + counter.count() + "\n");
                    writeToFile(bw, Clock.milliTick() + "," + counter.count() + "\n");
                    counter.reset();
                }
            }
        }
    }

    private void dumpTimers(Map<String, Timer> timers) {
        for(Timer timer : timers.values()) {
            BufferedWriter bw = this.statsFileWritersMap.get(timer.getFunctionName() + "." + timer.getTimerName());
            if(bw != null) {
                int buffered = 0 ;
                String bufferedData = "";
                List<TimeInstance> timeList = timer.getTimeList();
                for(TimeInstance timeInstance : timeList) {
                    bufferedData += timeInstance.getAtTime() + "," + timeInstance.getHowMuchTime() + "\n";
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
    }

    private void dumpHistograms(Map<String, Histogram> histograms) {
        for(Histogram histogram : histograms.values()) {
            BufferedWriter bw = this.statsFileWritersMap.get(histogram.getFunctionName() + "." + histogram.getName());
            if(bw != null) {
                int buffered = 0 ;
                String bufferedData = "";
                for(HistogramInstance histogramInstance : histogram.getInstances()) {
                    bufferedData += histogramInstance.getAtTime() + "," + histogramInstance.getValue() + "\n";
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
