package perf.server.daemon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.open.perf.constant.MathConstant;
import com.open.perf.util.Clock;
import com.open.perf.util.FileHelper;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.stats.Snapshot;
import org.apache.log4j.Logger;
import perf.server.config.JobFSConfig;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Compute both agent and overall timer files.
 */
public class TimerComputationThread extends Thread {

    private static ObjectMapper objectMapper;
    private final int checkInterval;
    private final JobFSConfig jobFSConfig;
    private boolean stop = false;
    private List<String> aliveJobs;

    private Map<String,FileTouchPoint> fileTouchPointMap;
    private Map<String,Long> fileAlreadyReadLinesMap;       // This would be further improved once i implement small file approach for big timer files
    private Map<String,Histogram> fileHistogramMap;       // This would be further improved once i implement small file approach for big timer files
    private Map<String,TimerStatsStamp> fileTimerStatsMap;       // This would be further improved once i implement small file approach for big timer files
    private Map<String,List> fileCachedContentMap;

    private static TimerComputationThread thread;
    private static Logger logger;
    private static final String FILE_EXTENSION;

    static {
        objectMapper = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("MMM dd hh:mm:ss z yyyy");
        objectMapper.setDateFormat(dateFormat);

        logger = Logger.getLogger(TimerComputationThread.class);
        FILE_EXTENSION = "stats";
    }

    private static class FileTouchPoint {
        private long lastModifiedTime;

        private FileTouchPoint(long lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
        }
        public boolean shouldReadFile(long newModifiedTime) {
            return this.lastModifiedTime != newModifiedTime ;
        }
    }

    /**
     * Used while crunching the data to remember the last crunching point
     */
    private class TimerStatsStamp {
        private long lastCount;
        private double lastSum;
        private long lastTimeNS;
        private long firstTimeNS;

        private TimerStatsStamp(long lastCount, double lastSum, long lastTimeNS) {
            this.lastCount = lastCount;
            this.lastTimeNS = lastTimeNS;
            this.lastSum = lastSum;
        }

        public TimerStatsStamp setFirstTimeNS(long firstTimeNS) {
            this.firstTimeNS = firstTimeNS;
            return this;
        }

        public TimerStatsStamp setLastCount(long lastCount) {
            this.lastCount = lastCount;
            return this;
        }

        public TimerStatsStamp setLastSum(double lastSum) {
            this.lastSum = lastSum;
            return this;
        }

        public TimerStatsStamp setLastTimeNS(long lastTimeNS) {
            this.lastTimeNS = lastTimeNS;
            return this;
        }
    }

    /**
     * Bean class to collect stats and write to file
     */
    private class TimerStatsInstance {
        private Date time;
        private long opsDone;
        private double min, max;
        private double dumpMean, dumpThroughput, overallMean, overAllThroughput, SD, fiftieth, seventyFifth, ninetieth, ninetyFifth, ninetyEight, ninetyNinth, nineNineNine;

        public Date getTime() {
            return time;
        }

        public TimerStatsInstance setTime(Date time) {
            this.time = time;
            return this;
        }

        public long getOpsDone() {
            return opsDone;
        }

        public TimerStatsInstance setOpsDone(long opsDone) {
            this.opsDone = opsDone;
            return this;
        }

        public double getMin() {
            return min;
        }

        public TimerStatsInstance setMin(double min) {
            this.min = min;
            return this;
        }

        public double getMax() {
            return max;
        }

        public TimerStatsInstance setMax(double max) {
            this.max = max;
            return this;
        }

        public double getDumpMean() {
            return dumpMean;
        }

        public TimerStatsInstance setDumpMean(double dumpMean) {
            this.dumpMean = dumpMean;
            return this;
        }

        public double getDumpThroughput() {
            return dumpThroughput;
        }

        public TimerStatsInstance setDumpThroughput(double dumpThroughput) {
            this.dumpThroughput = dumpThroughput;
            return this;
        }

        public double getOverallMean() {
            return overallMean;
        }

        public TimerStatsInstance setOverallMean(double overallMean) {
            this.overallMean = overallMean;
            return this;
        }

        public double getOverAllThroughput() {
            return overAllThroughput;
        }

        public TimerStatsInstance setOverAllThroughput(double overAllThroughput) {
            this.overAllThroughput = overAllThroughput;
            return this;
        }

        public double getSD() {
            return SD;
        }

        public TimerStatsInstance setSD(double SD) {
            this.SD = SD;
            return this;
        }

        public double getFiftieth() {
            return fiftieth;
        }

        public TimerStatsInstance setFiftieth(double fiftieth) {
            this.fiftieth = fiftieth;
            return this;
        }

        public double getSeventyFifth() {
            return seventyFifth;
        }

        public TimerStatsInstance setSeventyFifth(double seventyFifth) {
            this.seventyFifth = seventyFifth;
            return this;
        }

        public double getNinetieth() {
            return ninetieth;
        }

        public TimerStatsInstance setNinetieth(double ninetieth) {
            this.ninetieth = ninetieth;
            return this;
        }

        public double getNinetyFifth() {
            return ninetyFifth;
        }

        public TimerStatsInstance setNinetyFifth(double ninetyFifth) {
            this.ninetyFifth = ninetyFifth;
            return this;
        }

        public double getNinetyEight() {
            return ninetyEight;
        }

        public TimerStatsInstance setNinetyEight(double ninetyEight) {
            this.ninetyEight = ninetyEight;
            return this;
        }

        public double getNinetyNinth() {
            return ninetyNinth;
        }

        public TimerStatsInstance setNinetyNinth(double ninetyNinth) {
            this.ninetyNinth = ninetyNinth;
            return this;
        }

        public double getNineNineNine() {
            return nineNineNine;
        }

        public TimerStatsInstance setNineNineNine(double nineNineNine) {
            this.nineNineNine = nineNineNine;
            return this;
        }
    }

    private TimerComputationThread(JobFSConfig jobFSConfig, int checkInterval) {
        this.jobFSConfig = jobFSConfig;
        this.checkInterval = checkInterval;
        this.aliveJobs = new ArrayList<String>();
        this.fileTouchPointMap = new HashMap<String, FileTouchPoint>();
        this.fileAlreadyReadLinesMap = new HashMap<String, Long>();
        this.fileHistogramMap = new HashMap<String, Histogram>();
        this.fileTimerStatsMap = new HashMap<String, TimerStatsStamp>();
        this.fileCachedContentMap = new HashMap<String, List>();
    }

    public static TimerComputationThread initialize(JobFSConfig jobFSConfig, int checkInterval) {
        if(thread == null) {
            thread = new TimerComputationThread(jobFSConfig, checkInterval);
        }
        return thread;
    }

    public static TimerComputationThread getComputationThread() {
        return thread;
    }

    public void run() {
        while(keepRunning()) {
            synchronized (this.aliveJobs) {
                for(String jobId : this.aliveJobs) {
                    try {
                        crunchJobTimers(jobId);
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
            Clock.sleep(granularSleep);
            totalInterval += granularSleep;
        }
    }

    private void crunchJobTimers(String jobId) throws IOException {
        List<File> jobFiles = FileHelper.pathFiles(this.jobFSConfig.getJobPath(jobId), true);
        for(File jobFile : jobFiles) {
            if(jobFile.getAbsolutePath().contains("timer") && !jobFile.getAbsolutePath().contains("stats")) {
                crunchJobFileTimer(jobId, jobFile);
            }
        }
    }

    synchronized public void crunchJobFileTimer(String jobId, File jobFile) throws IOException {
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
            cachedContent = new LinkedList<String>();
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
        if(canParseContentNow(jobId, cachedContent)) {

            BufferedWriter bw = FileHelper.bufferedWriter(jobFile.getAbsolutePath()+"."+FILE_EXTENSION, true);

            // Last Timer Stats
            TimerStatsStamp timerStatsStamp = this.fileTimerStatsMap.get(jobFile.getAbsolutePath());
            if(timerStatsStamp == null) {
                String currentLine = cachedContent.remove(0);
                String[] tokens = currentLine.split(",");
                timerStatsStamp = new TimerStatsStamp(1, Double.parseDouble(tokens[1]), Long.parseLong(tokens[0])).
                        setFirstTimeNS(Long.parseLong(tokens[0]));
                histogram.update((long) Double.parseDouble(tokens[1]));
            }

            int countInThisIteration = 0;
            while(cachedContent.size() > 0) {
                String currentLine = cachedContent.remove(0);
                countInThisIteration++;

                StringTokenizer tokenizer = new StringTokenizer(currentLine, ",");
                long lineTimeNS = Long.parseLong(tokenizer.nextElement().toString());
                double lineResponseTimeNS = Double.parseDouble(tokenizer.nextElement().toString());

                if((Clock.nsTick() - lineResponseTimeNS) > MathConstant.BILLION && !jobOver(jobId)) {
                    cachedContent.add(0, currentLine);
                    break;
                }

                histogram.update((long)lineResponseTimeNS);

                // Either you have collected 1 million instances or you have collected data worth 10 seconds
                TimerStatsInstance timerStatsInstance;
                if(countInThisIteration % MathConstant.MILLION == 0 ||
                        (lineTimeNS - timerStatsStamp.lastTimeNS) > 10 * MathConstant.BILLION ||
                        (jobOver(jobId) && cachedContent.size() == 0)) {

                    double iterationTimeSec = (lineTimeNS - timerStatsStamp.lastTimeNS) / (float)MathConstant.BILLION;
                    double iterationThroughputSec = countInThisIteration/iterationTimeSec;
                    double overallThroughputSec = histogram.count()/((lineTimeNS - timerStatsStamp.firstTimeNS) / (float)MathConstant.BILLION);
                    double iterationMean = (histogram.sum() - timerStatsStamp.lastSum)/countInThisIteration;

                    Snapshot snapshot = histogram.getSnapshot();
                    timerStatsInstance = new TimerStatsInstance().
                            setMin(histogram.min() / MathConstant.MILLION).
                            setMax(histogram.max() / MathConstant.MILLION).
                            setDumpMean(iterationMean / MathConstant.MILLION).
                            setDumpThroughput(iterationThroughputSec).
                            setOpsDone(histogram.count()).
                            setOverallMean(histogram.mean() / MathConstant.MILLION).
                            setOverAllThroughput(overallThroughputSec).
                            setSD(histogram.stdDev() / MathConstant.MILLION).
                            setFiftieth(snapshot.getValue(0.50) / MathConstant.MILLION).
                            setSeventyFifth(snapshot.get75thPercentile() / MathConstant.MILLION).
                            setNinetieth(snapshot.getValue(0.90) / MathConstant.MILLION).
                            setNinetyFifth(snapshot.get95thPercentile() / MathConstant.MILLION).
                            setNinetyEight(snapshot.get98thPercentile() / MathConstant.MILLION).
                            setNinetyNinth(snapshot.get99thPercentile() / MathConstant.MILLION).
                            setNineNineNine(snapshot.get999thPercentile() / MathConstant.MILLION).
                            setTime(Clock.nsToSec(lineTimeNS));

                    bw.write(objectMapper.writeValueAsString(timerStatsInstance) + "\n");
                    bw.flush();
                    BufferedWriter bwLast = FileHelper.bufferedWriter(jobFile.getAbsolutePath()+"."+FILE_EXTENSION+".last", false);
                    bwLast.write(objectMapper.writeValueAsString(timerStatsInstance) + "\n");
                    bwLast.flush();
                    FileHelper.close(bwLast);
                    timerStatsStamp.
                            setLastCount(histogram.count()).
                            setLastSum(histogram.sum()).
                            setLastTimeNS(lineTimeNS);
                    countInThisIteration = 0;
                }

            }
            this.fileTimerStatsMap.put(jobFile.getAbsolutePath(), timerStatsStamp);
            FileHelper.close(bw);
        }
        FileHelper.close(br);
    }

    private boolean canParseContentNow(String jobId, List<String> cachedContent) {
        if(cachedContent.size() == 0)
            return false;
        String lastLine = cachedContent.get(cachedContent.size()-1);
        StringTokenizer tokenizer = new StringTokenizer(lastLine, ",");
        long lineTimeNS = Long.parseLong(tokenizer.nextElement().toString());
        long currentTimeNS = Clock.nsTick();
        return (currentTimeNS - lineTimeNS) > (60 + 30) * MathConstant.BILLION || jobOver(jobId); // Crunch if data is older than 60 + 30 seconds
    }

    private boolean keepRunning() {
        return !stop;
    }

    public void stopIt() {
        stop = true;
    }

    synchronized public void addJob(String jobId) {
        this.aliveJobs.add(jobId);
    }

    synchronized public void removeJob(String jobId) throws IOException {
        this.aliveJobs.remove(jobId);
        crunchJobTimers(jobId);
    }

    private boolean jobOver(String jobId) {
        return !this.aliveJobs.contains(jobId);
    }

    public static void main(String[] args) throws IOException {
        TimerComputationThread t = new TimerComputationThread(null, 10000);
        long startTime = Clock.nsTick();
        t.crunchJobFileTimer("", new File("/var/log/loader-server/jobs/eb9743df-0dfe-4d34-84fe-04054cee8b9d/agents/127.0.0.1/jobStats/SampleGroup/timers/DummyFunction"));
        System.out.println(Clock.nsTick() - startTime);
    }
}