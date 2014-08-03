package com.flipkart.perf.server.daemon;

import com.flipkart.perf.common.constant.MathConstant;
import com.flipkart.perf.common.util.Clock;
import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.server.config.JobFSConfig;
import com.flipkart.perf.server.domain.HistogramStatsInstance;
import com.flipkart.perf.server.domain.HistogramStatsInstance;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.stats.Snapshot;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compute both agent and overall timer files.
 */
public class HistogramComputationThread extends Thread {

    private static ObjectMapper objectMapper;
    private final int checkInterval;
    private final JobFSConfig jobFSConfig;
    private boolean stop = false;
    private List<String> aliveJobs;

    private Map<String,Long> fileAlreadyReadLinesMap;       // This would be further improved once i implement small file approach for big timer files
    private Map<String,Histogram> fileHistogramMap;       // This would be further improved once i implement small file approach for big timer files
    private Map<String,HistogramStatsStamp> fileTimerStatsMap;       // This would be further improved once i implement small file approach for big timer files
    private Map<String,List> fileCachedContentMap;

    private static volatile HistogramComputationThread thread;
    private static Logger logger;
    private static final String FILE_EXTENSION;

    static {
        objectMapper = ObjectMapperUtil.instance();
        logger = LoggerFactory.getLogger(HistogramComputationThread.class);
        FILE_EXTENSION = "stats";
    }

    /**
     * Used while crunching the data to remember the last crunching point
     */
    public static class HistogramStatsStamp {
        private long lastCount;
        private double lastSum;
        private long lastTimeMS;
        private long firstTimeMS;

        private HistogramStatsStamp(long lastCount, double lastSum, long lastTimeMS) {
            this.lastCount = lastCount;
            this.lastTimeMS = lastTimeMS;
            this.lastSum = lastSum;
        }

        public HistogramStatsStamp setFirstTimeMS(long firstTimeMS) {
            this.firstTimeMS = firstTimeMS;
            return this;
        }

        public HistogramStatsStamp setLastCount(long lastCount) {
            this.lastCount = lastCount;
            return this;
        }

        public HistogramStatsStamp setLastSum(double lastSum) {
            this.lastSum = lastSum;
            return this;
        }

        public HistogramStatsStamp setLastTimeMS(long lastTimeMS) {
            this.lastTimeMS = lastTimeMS;
            return this;
        }
    }

    private HistogramComputationThread(JobFSConfig jobFSConfig, int checkInterval) {
        this.jobFSConfig = jobFSConfig;
        this.checkInterval = checkInterval;
        this.aliveJobs = new ArrayList<String>();
        this.fileAlreadyReadLinesMap = new ConcurrentHashMap<String, Long>();
        this.fileHistogramMap = new ConcurrentHashMap<String, Histogram>();
        this.fileTimerStatsMap = new ConcurrentHashMap<String, HistogramStatsStamp>();
        this.fileCachedContentMap = new ConcurrentHashMap<String, List>();
    }

    public static HistogramComputationThread initialize(JobFSConfig jobFSConfig, int checkInterval) {
        if(thread == null) {
        	synchronized(HistogramComputationThread.class) {
        		thread = new HistogramComputationThread(jobFSConfig, checkInterval);
        	}
        }
        return thread;
    }

    public static HistogramComputationThread instance() {
        return thread;
    }

    public void run() {
        while(keepRunning()) {
            synchronized (this.aliveJobs) {
                for(String jobId : this.aliveJobs) {
                    try {
                        crunchJobHistogram(jobId);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
            checkInterval();
        }
    }

    private void checkInterval() {
        logger.debug("Sleeping for " + this.checkInterval + "ms");
        int totalInterval = 0;
        int granularSleep = 200;
        while(totalInterval < this.checkInterval && !this.stop) {
            try {
                Clock.sleep(granularSleep);
                totalInterval += granularSleep;
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void crunchJobHistogram(String jobId) throws IOException {
        List<File> jobFiles = FileHelper.pathFiles(this.jobFSConfig.getJobStatsPath(jobId), true);
        for(File jobFile : jobFiles) {
            if(jobFile.getAbsolutePath().contains("histogram") && !jobFile.getAbsolutePath().contains("stats")) {
                crunchJobFileHistogram(jobId, jobFile);
            }
        }
    }

    synchronized public void crunchJobFileHistogram(String jobId, File jobFile) throws IOException {
        BufferedReader br = FileHelper.bufferedReader(jobFile.getAbsolutePath());

        // Skip the number of lines that are already read from this file
        long alreadyReadLines = 0;
        if(this.fileAlreadyReadLinesMap.containsKey(jobFile.getAbsolutePath()))
            alreadyReadLines = this.fileAlreadyReadLinesMap.get(jobFile.getAbsolutePath());

        for(long i=0; i< alreadyReadLines; i++)
            br.readLine(); // Skipping already read lines

        // Populate remaining content in a list
        List<String> cachedContent = this.fileCachedContentMap.get(jobFile.getAbsolutePath());
        if(cachedContent == null) {
            cachedContent = Collections.synchronizedList(new ArrayList<String>());
            this.fileCachedContentMap.put(jobFile.getAbsolutePath(), cachedContent);
        }
        String line = null;
        while((line = br.readLine()) != null) {
            cachedContent.add(line);
            alreadyReadLines++;
        }
        this.fileAlreadyReadLinesMap.put(jobFile.getAbsolutePath(), alreadyReadLines);


        // Sort the collected stats.
        Collections.sort(cachedContent);

        // Using Histogram class to compute mean, SD, Variance and Percentiles
        Histogram histogram = this.fileHistogramMap.get(jobFile.getAbsolutePath());
        if(histogram == null) {
            histogram = Metrics.newHistogram(new MetricName("G"+System.nanoTime(), "T"+System.nanoTime(), "N"+System.nanoTime()), false);
        }

        // Iterate and compute
        if(canParseContentNow(jobId, cachedContent, isAgentStats(jobFile))) {

            BufferedWriter bw = FileHelper.bufferedWriter(jobFile.getAbsolutePath()+"."+FILE_EXTENSION, true);

            // Last Timer Stats
            HistogramStatsStamp histogramStatsStamp = this.fileTimerStatsMap.get(jobFile.getAbsolutePath());
            if(histogramStatsStamp == null) {
                String currentLine = cachedContent.remove(0);
                String[] tokens = currentLine.split(",");
                histogramStatsStamp = new HistogramStatsStamp(1, Double.parseDouble(tokens[1]), Long.parseLong(tokens[0])).
                        setFirstTimeMS(Long.parseLong(tokens[0]));
                histogram.update((long) Double.parseDouble(tokens[1]));

                // Group in performance run had only one repeat
                if(cachedContent.size() == 0 && jobOver(jobId)) {

                    Snapshot snapshot = histogram.getSnapshot();
                    HistogramStatsInstance histogramStatsInstance = new HistogramStatsInstance().
                            setMin(histogram.min()).
                            setMax(histogram.max()).
                            setDumpMean(histogram.mean()).
                            setOpsDone(histogram.count()).
                            setOverallMean(histogram.mean()).
                            setSD(histogram.stdDev()).
                            setFiftieth(snapshot.getValue(0.50)).
                            setSeventyFifth(snapshot.get75thPercentile()).
                            setNinetieth(snapshot.getValue(0.90)).
                            setNinetyFifth(snapshot.get95thPercentile()).
                            setNinetyEight(snapshot.get98thPercentile()).
                            setNinetyNinth(snapshot.get99thPercentile()).
                            setNineNineNine(snapshot.get999thPercentile()).
                            setTime(Clock.dateFromMS(Long.parseLong(tokens[0])));

                    bw.write(objectMapper.writeValueAsString(histogramStatsInstance) + "\n");
                    bw.flush();
                    BufferedWriter bwLast = FileHelper.bufferedWriter(jobFile.getAbsolutePath()+"."+FILE_EXTENSION+".last", false);
                    bwLast.write(objectMapper.writeValueAsString(histogramStatsInstance) + "\n");
                    bwLast.flush();
                    FileHelper.close(bwLast);
                }
            }

            while(cachedContent.size() > 0) {
                String currentLine = cachedContent.remove(0);

                StringTokenizer tokenizer = new StringTokenizer(currentLine, ",");
                long lineTimeMS = Long.parseLong(tokenizer.nextElement().toString());
                double lineResponseTimeNS = Double.parseDouble(tokenizer.nextElement().toString());

                /**
                 * Recently Added code. Check the functionality
                 */
                if (shouldBreak(jobId, lineTimeMS, isAgentStats(jobFile))) {
                    cachedContent.add(0, currentLine);
                    break;
                }

                histogram.update((long)lineResponseTimeNS);

                // Either you have collected 1 million instances or you have collected data worth 10 seconds
                HistogramStatsInstance histogramStatsInstance;
                long countSinceLastCalculation = histogram.count() - histogramStatsStamp.lastCount;
                if(countSinceLastCalculation % MathConstant.MILLION == 0 ||
                        (lineTimeMS - histogramStatsStamp.lastTimeMS) > 10 * MathConstant.THOUSAND ||
                        (jobOver(jobId) && cachedContent.size() == 0)) {

                    double iterationMean = (histogram.sum() - histogramStatsStamp.lastSum)/countSinceLastCalculation;

                    Snapshot snapshot = histogram.getSnapshot();
                    histogramStatsInstance = new HistogramStatsInstance().
                            setMin(histogram.min()).
                            setMax(histogram.max()).
                            setDumpMean(iterationMean).
                            setOpsDone(histogram.count()).
                            setOverallMean(histogram.mean()).
                            setSD(histogram.stdDev()).
                            setFiftieth(snapshot.getValue(0.50)).
                            setSeventyFifth(snapshot.get75thPercentile()).
                            setNinetieth(snapshot.getValue(0.90)).
                            setNinetyFifth(snapshot.get95thPercentile()).
                            setNinetyEight(snapshot.get98thPercentile()).
                            setNinetyNinth(snapshot.get99thPercentile()).
                            setNineNineNine(snapshot.get999thPercentile()).
                            setTime(Clock.dateFromMS(lineTimeMS));

                    bw.write(objectMapper.writeValueAsString(histogramStatsInstance) + "\n");
                    bw.flush();
                    BufferedWriter bwLast = FileHelper.bufferedWriter(jobFile.getAbsolutePath()+"."+FILE_EXTENSION+".last", false);
                    bwLast.write(objectMapper.writeValueAsString(histogramStatsInstance) + "\n");
                    bwLast.flush();
                    FileHelper.close(bwLast);
                    histogramStatsStamp.
                            setLastCount(histogram.count()).
                            setLastSum(histogram.sum()).
                            setLastTimeMS(lineTimeMS);
                }

            }
            this.fileTimerStatsMap.put(jobFile.getAbsolutePath(), histogramStatsStamp);
            this.fileHistogramMap.put(jobFile.getAbsolutePath(), histogram);
            FileHelper.close(bw);
        }
        FileHelper.close(br);
    }

    private boolean isAgentStats(File jobFile) {
        return !jobFile.getAbsolutePath().contains("combined");
    }

    /**
     * If stepped on data which is in last 60 seconds.
     * @param jobId
     * @param lineTimeMS
     * @return
     */
    private boolean shouldBreak(String jobId, double lineTimeMS, boolean isAgentStats) {
        return ((Clock.milliTick() - lineTimeMS) < (isAgentStats ? 20 : 60) * MathConstant.THOUSAND) && !jobOver(jobId);
    }

    private boolean canParseContentNow(String jobId, List<String> cachedContent, boolean isAgentStats) {
        if(cachedContent.size() == 0)
            return false;
        String firstLine = cachedContent.get(0);
        StringTokenizer firstLineTokenizer = new StringTokenizer(firstLine, ",");
        long firstLineTimeMS = Long.parseLong(firstLineTokenizer.nextElement().toString());
        long currentTimeMS = Clock.milliTick();
        logger.info("First Lines in Cached Content is  :"+(currentTimeMS - firstLineTimeMS)+"ms old");

        String lastLine = cachedContent.get(cachedContent.size()-1);
        StringTokenizer lastLineTokenizer = new StringTokenizer(lastLine, ",");
        long lastLineTimeMS = Long.parseLong(lastLineTokenizer.nextElement().toString());
        logger.info("Cached Content has data for  :"+(lastLineTimeMS - firstLineTimeMS)+"ms time");

        return ((currentTimeMS - firstLineTimeMS) > ((isAgentStats ? 20 : 60) + 30) * MathConstant.THOUSAND) || jobOver(jobId); // Crunch if data is older than 60 + 30 seconds
    }

    private boolean keepRunning() {
        return !stop;
    }

    public void stopIt() {
        stop = true;
    }

    public void addJob(String jobId) {
        synchronized (this.aliveJobs) {
            this.aliveJobs.add(jobId);
        }
    }

    public void removeJob(String jobId) throws IOException {
        synchronized (this.aliveJobs) {
            this.aliveJobs.remove(jobId);
            crunchJobHistogram(jobId);
        }
    }

    private boolean jobOver(String jobId) {
        return !this.aliveJobs.contains(jobId);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("" + (long)Double.parseDouble("2.636512277E9"));
/*
        TimerComputationThread t = new TimerComputationThread(null, 10000);
        long startTime = Clock.nsTick();
        t.crunchJobFileHistogram("", new File("/home/nitinka/git/loader2.0/loader-server/bug/data"));
*/
    }
}