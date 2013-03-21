package perf.server.daemon;

import com.open.perf.constant.MathConstant;
import com.open.perf.util.Clock;
import com.open.perf.util.FileHelper;
import org.apache.log4j.Logger;
import perf.server.config.JobFSConfig;

import java.io.*;
import java.util.*;

/**
 * Just sum up the counters and write to counter.cumulative
 */
public class CounterCompoundThread extends Thread {

    private final int checkInterval;
    private final JobFSConfig jobFSConfig;
    private boolean stop = false;
    private List<String> aliveJobs;

    private Map<String,List<String>> fileCachedContentMap; // Cached Content per counter throughput file
    private Map<String,FileTouchPoint> fileTouchPointMap;
    private Map<String,LastCrunchPoint> fileLastCrunchPointMap;

    private static CounterCompoundThread thread;
    private static final long CLUB_CRUNCH_DURATION; // Club and crunch duration to calculate throughput
    private static final long CRUNCH_DATA_OLDER_THAN; // As long as job is alive crunch data which is older than 30 secs
    private static Logger logger;
    private static final String FILE_EXTENSION;

    static {
        CLUB_CRUNCH_DURATION = 10 * MathConstant.BILLION; // Club and crunch duration to calculate throughput
        CRUNCH_DATA_OLDER_THAN = 30 * MathConstant.BILLION; // As long as job is alive crunch data which is older than 30 secs
        logger = Logger.getLogger(CounterCompoundThread.class);
        FILE_EXTENSION = "cumulative";
    }


    private class CounterStatsInstance {
        private Date time;
        private long count;

        public Date getTime() {
            return time;
        }

        public CounterStatsInstance setTime(Date time) {
            this.time = time;
            return this;
        }

        public long getCount() {
            return count;
        }

        public CounterStatsInstance setCount(long count) {
            this.count = count;
            return this;
        }
    }

    private static class FileTouchPoint {
        private long lastModifiedTime;
        private long lastReadPoint;

        private FileTouchPoint(long lastModifiedTime, long lastReadPoint) {
            this.lastModifiedTime = lastModifiedTime;
            this.lastReadPoint = lastReadPoint;
        }
        public boolean shouldReadFile(long newModifiedTime) {
            return this.lastModifiedTime != newModifiedTime ;
        }
    }

    private static class LastCrunchPoint {
        private long time;
        private long countSoFar;

        private LastCrunchPoint(long time, long countSoFar) {
            this.time = time;
            this.countSoFar = countSoFar;
        }
    }

    private CounterCompoundThread(JobFSConfig jobFSConfig, int checkInterval) {
        this.jobFSConfig = jobFSConfig;
        this.checkInterval = checkInterval;
        this.aliveJobs = new ArrayList<String>();
        this.fileCachedContentMap = new HashMap<String, List<String>>();
        this.fileTouchPointMap = new HashMap<String, FileTouchPoint>();
        this.fileLastCrunchPointMap = new HashMap<String, LastCrunchPoint>();
    }

    public static CounterCompoundThread initialize(JobFSConfig jobFSConfig, int checkInterval) {
        if(thread == null) {
            thread = new CounterCompoundThread(jobFSConfig, checkInterval);
        }
        return thread;
    }

    public static CounterCompoundThread getCounterCruncherThread() {
        return thread;
    }

    public void run() {
        while(keepRunning()) {
            synchronized (this.aliveJobs) {
                for(String jobId : this.aliveJobs) {
                    crunchJobCounters(jobId);
                }
            }
            checkInterval();
        }
    }

    private void crunchJobCounters(String jobId) {
        logger.info("Crunching Counters for Job Id :"+jobId);
        List<File> jobFiles = FileHelper.pathFiles(this.jobFSConfig.getJobPath(jobId), true);
        for(File jobFile : jobFiles) {
            if(jobFile.getAbsolutePath().contains("counter")
                    && !jobFile.getAbsolutePath().contains(FILE_EXTENSION)
                    && !jobFile.getAbsolutePath().contains("throughput")) {
                crunchJobFileCounter(jobId, jobFile);
            }
        }
    }

    public void crunchJobFileCounter(String jobId, File jobFile) {
        logger.info("Crunching File :"+jobFile.getAbsolutePath() + " for job id :" + jobId);
        List<String> fileContentLines = readFileContentAsList(jobFile);

        logger.info("New Content Size :"+fileContentLines.size());
        List<String> cachedContent = this.fileCachedContentMap.get(jobFile.getAbsolutePath());
        if(cachedContent == null) {
            cachedContent = new ArrayList<String>();
        }
        cachedContent.addAll(fileContentLines);
        Collections.sort(cachedContent);
        logger.info("Cached Content Size :"+cachedContent.size());

        if(cachedContent.size() > 0) {
            String newFile = jobFile.getAbsolutePath() + "." + FILE_EXTENSION;
            BufferedWriter bw = null;
            try {
                bw = FileHelper.bufferedWriter(newFile, true);
                long firstEntryTime = Long.parseLong(cachedContent.get(0).split(",")[0]);
                long lastEntryTime = Long.parseLong(cachedContent.get(cachedContent.size()-1).split(",")[0]);
                logger.info("Job Over :"+jobOver(jobId));
                logger.info("Data Duration in cached content :"+ (float)(lastEntryTime - firstEntryTime)/MathConstant.BILLION + " seconds");

                List<String> dataToCrunch = new ArrayList<String>();
                if(jobOver(jobId)) {

                    logger.info("Job is over");
                    dataToCrunch.addAll(cachedContent);
                    logger.info("Data To Crunch :"+dataToCrunch.size());
                    cachedContent.clear();
                    logger.info("Data To Crunch :"+dataToCrunch.size());
                }
                else if((lastEntryTime - firstEntryTime) > (CRUNCH_DATA_OLDER_THAN + CLUB_CRUNCH_DURATION)){
                    while(cachedContent.size() > 0) {
                        String cachedContentLine = cachedContent.remove(0);
                        String[] tokens = cachedContentLine.split(",");
                        long currentContentTime = Long.parseLong(tokens[0]);
                        if(lastEntryTime - currentContentTime < CRUNCH_DATA_OLDER_THAN) {
                            cachedContent.add(0, cachedContentLine);
                            break;
                        }
                        dataToCrunch.add(cachedContentLine);
                    }
                }

                if(dataToCrunch.size() > 0) {
                    LastCrunchPoint lastCrunchPoint = this.fileLastCrunchPointMap.get(jobFile.getAbsolutePath());
                    if(lastCrunchPoint == null) {
                        String firstContentLine = dataToCrunch.remove(0);
                        String[] tokens = firstContentLine.split(",");
                        lastCrunchPoint = new LastCrunchPoint(Long.parseLong(tokens[0]),
                                Long.parseLong(tokens[1]));
                        bw.write(tokens[0] + ",0\n");
                        bw.flush();
                    }

                    long opsDone = 0;

                    while(dataToCrunch.size() > 0) {

                        while(dataToCrunch.size() > 0) {
                            String dataToCrunchLine = dataToCrunch.remove(0);
                            String[] tokens = dataToCrunchLine.split(",");
                            long currentContentTime = Long.parseLong(tokens[0]);
                            long currentContentCount = Long.parseLong(tokens[1]);

                            // Collect Content To Crunch
                            opsDone += currentContentCount;

                            // Next Content Time
                            long nextContentTime = -1;
                            if(dataToCrunch.size() > 0) {
                                nextContentTime = Long.parseLong(dataToCrunch.get(0).split(",")[0]);
                            }

                            // Crunch if collected data for 10 seconds have been collected and next Content Time is different
                            if((currentContentTime - lastCrunchPoint.time > CLUB_CRUNCH_DURATION && currentContentTime != nextContentTime )
                                    || nextContentTime == -1 ) {
                                long totalOpsDoneSoFar = lastCrunchPoint.countSoFar + opsDone;
                                lastCrunchPoint = new LastCrunchPoint(currentContentTime, totalOpsDoneSoFar);
                                this.fileLastCrunchPointMap.put(jobFile.getAbsolutePath(), lastCrunchPoint);

                                bw.write(lastCrunchPoint.time + "," + lastCrunchPoint.countSoFar + "\n");
                                bw.flush();
                                opsDone = 0;
                            }
                        }
                    }
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } finally {
                try {
                    FileHelper.close(bw);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        logger.info("Cached Content Left :"+cachedContent.size());
        this.fileCachedContentMap.put(jobFile.getAbsolutePath(), cachedContent);
    }

    private boolean jobOver(String jobId) {
        return !this.aliveJobs.contains(jobId);
    }

    private List<String> readFileContentAsList(File jobFile) {
        List<String> lines = new ArrayList<String>();
        FileTouchPoint fileTouchPoint = this.fileTouchPointMap.get(jobFile.getAbsolutePath());

        boolean needToReadFile = false;
        long lastReadPoint = -1;
        if(fileTouchPoint == null) {
            needToReadFile = true;
        }
        else if(fileTouchPoint.shouldReadFile(jobFile.lastModified())) {
            needToReadFile = true;
            lastReadPoint = fileTouchPoint.lastReadPoint;
        }

        if(needToReadFile) {
            RandomAccessFile raf = null;
            try {
                raf = FileHelper.randomAccessFile(jobFile, "r");

                if(lastReadPoint != -1)
                    raf.seek(lastReadPoint);

                String line = null;
                while((line = raf.readLine()) != null) {
                    lines.add(line);
                }
                lastReadPoint = raf.getFilePointer();
                fileTouchPointMap.put(jobFile.getAbsolutePath(), new FileTouchPoint(jobFile.lastModified(), lastReadPoint));


            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            finally {
                if(raf != null)
                    try {
                        FileHelper.close(raf);
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
            }

        }
        return lines;
    }


    private void checkInterval() {
        logger.debug("Sleeping for "+this.checkInterval+"ms");
        int totalInterval = 0;
        int granularSleep = 200;
        while(totalInterval < this.checkInterval && !this.stop) {
            Clock.sleep(granularSleep);
            totalInterval += granularSleep;
        }
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

    synchronized public void removeJob(String jobId) {
        this.aliveJobs.remove(jobId);
        crunchJobCounters(jobId);
    }

    public static void main(String[] args) {
        CounterCompoundThread t = new CounterCompoundThread(null, 10);
        t.crunchJobFileCounter("", new File("/var/log/loader-server/jobs/a09cc4f7-e868-4b2c-ae55-d9e992c2bd46/jobStats/SampleGroup/counters/tmp1"));

    }
}