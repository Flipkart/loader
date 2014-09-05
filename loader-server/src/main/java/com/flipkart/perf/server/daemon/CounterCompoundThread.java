package com.flipkart.perf.server.daemon;

import com.flipkart.perf.common.constant.MathConstant;
import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.config.FSConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.perf.server.config.JobFSConfig;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Just sum up the counters and write to counter.cumulative
 */
public class CounterCompoundThread extends Thread {

    private final JobFSConfig jobFSConfig;
    private List<String> aliveJobs;

    private Map<String,List<String>> fileCachedContentMap; // Cached Content per counter throughput file
    private Map<String,FileTouchPoint> fileTouchPointMap;
    private Map<String,LastCrunchPoint> fileLastCrunchPointMap;

    private static volatile CounterCompoundThread thread;
    private static final long CLUB_CRUNCH_DURATION_MS; // Club and crunch duration to calculate throughput
    private static final long CRUNCH_DATA_OLDER_THAN_MS; // As long as job is alive crunch data which is older than 30 secs
    private static Logger logger;
    private static final String FILE_EXTENSION;

    static {
        CLUB_CRUNCH_DURATION_MS = 10 * MathConstant.THOUSAND;
        CRUNCH_DATA_OLDER_THAN_MS = 30 * MathConstant.THOUSAND;
        logger = LoggerFactory.getLogger(CounterCompoundThread.class);
        FILE_EXTENSION = "stats";
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

    private CounterCompoundThread(JobFSConfig jobFSConfig) {
        this.jobFSConfig = jobFSConfig;
        this.aliveJobs = new ArrayList<String>();
        this.fileCachedContentMap = new ConcurrentHashMap<String, List<String>>();
        this.fileTouchPointMap = new ConcurrentHashMap<String, FileTouchPoint>();
        this.fileLastCrunchPointMap = new ConcurrentHashMap<String, LastCrunchPoint>();
    }

    public static CounterCompoundThread initialize(ScheduledExecutorService scheduledExecutorService, JobFSConfig jobFSConfig, int interval) {
        if(thread == null) {
        	synchronized(CounterCompoundThread.class) {
        		thread = new CounterCompoundThread(jobFSConfig);
        	}
            scheduledExecutorService.scheduleWithFixedDelay(thread,
                    1000,
                    interval,
                    TimeUnit.MILLISECONDS);
        }
        return thread;
    }

    public static CounterCompoundThread instance() {
        return thread;
    }

    public void run() {
        try {
            synchronized (this.aliveJobs) {
                for(String jobId : this.aliveJobs) {
                    crunchJobCounters(jobId);
                }
            }
        } catch (Exception e) {
            logger.error("Error while crunching counters", e);
        }
    }

    private void crunchJobCounters(String jobId) {
        List<File> jobFiles = FileHelper.pathFiles(this.jobFSConfig.getJobPath(jobId), true);
        for(File jobFile : jobFiles) {
            if(jobFile.getAbsolutePath().contains("counter")
                    && !jobFile.getAbsolutePath().contains(FILE_EXTENSION)) {
                crunchJobFileCounter(jobId, jobFile);
            }
        }
    }

    synchronized public void crunchJobFileCounter(String jobId, File jobFile) {
        List<String> fileContentLines = readFileContentAsList(jobFile);

        List<String> cachedContent = this.fileCachedContentMap.get(jobFile.getAbsolutePath());
        if(cachedContent == null) {
            cachedContent = new ArrayList<String>();
        }
        cachedContent.addAll(fileContentLines);
        Collections.sort(cachedContent);

        if(cachedContent.size() > 0) {
            String newFile = jobFile.getAbsolutePath() + "." + FILE_EXTENSION;
            BufferedWriter bw = null;
            try {
                bw = FileHelper.bufferedWriter(newFile, true);
                long firstEntryTimeMS = Long.parseLong(cachedContent.get(0).split(",")[0]);
                long lastEntryTimeMS = Long.parseLong(cachedContent.get(cachedContent.size()-1).split(",")[0]);

                List<String> dataToCrunch = new ArrayList<String>();
                if(jobOver(jobId)) {
                    dataToCrunch.addAll(cachedContent);
                    cachedContent.clear();
                }
                else if((lastEntryTimeMS - firstEntryTimeMS) > (CRUNCH_DATA_OLDER_THAN_MS + CLUB_CRUNCH_DURATION_MS)){
                    while(cachedContent.size() > 0) {
                        String cachedContentLine = cachedContent.remove(0);
                        String[] tokens = cachedContentLine.split(",");
                        long currentContentTime = Long.parseLong(tokens[0]);
                        if(lastEntryTimeMS - currentContentTime < CRUNCH_DATA_OLDER_THAN_MS) {
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
                        bw.write("{\"time\" :" +tokens[0] + ",\"count\" :0}\n");
                        bw.flush();
                    }

                    long opsDone = 0;

                    while(dataToCrunch.size() > 0) {
                        String dataToCrunchLine = dataToCrunch.remove(0);
                        String[] tokens = dataToCrunchLine.split(",");
                        long currentContentTimeMS = Long.parseLong(tokens[0]);
                        long currentContentCount = Long.parseLong(tokens[1]);

                        // Collect Content To Crunch
                        opsDone += currentContentCount;

                        // Next Content Time
                        long nextContentTimeMS = -1;
                        if(dataToCrunch.size() > 0) {
                            nextContentTimeMS = Long.parseLong(dataToCrunch.get(0).split(",")[0]);
                        }

                        // Crunch if collected data for 10 seconds have been collected and next Content Time is different
                        if((currentContentTimeMS - lastCrunchPoint.time > CLUB_CRUNCH_DURATION_MS && currentContentTimeMS != nextContentTimeMS )
                                || nextContentTimeMS == -1 ) {
                            long totalOpsDoneSoFar = lastCrunchPoint.countSoFar + opsDone;
                            lastCrunchPoint = new LastCrunchPoint(currentContentTimeMS, totalOpsDoneSoFar);
                            this.fileLastCrunchPointMap.put(jobFile.getAbsolutePath(), lastCrunchPoint);
                            bw.write("{\"time\" :" +lastCrunchPoint.time + ",\"count\" :"+lastCrunchPoint.countSoFar+"}\n");
                            bw.flush();
                            opsDone = 0;
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

        this.fileCachedContentMap.put(jobFile.getAbsolutePath(), cachedContent);
    }

    private boolean jobOver(String jobId) {
        return !this.aliveJobs.contains(jobId);
    }

    // TBD Remove Random Access File Usage. Its too slow. Make use of BufferedReader smartly.
    // Over usage of BR would still be faster than RAF
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

    synchronized public void addJob(String jobId) {
        this.aliveJobs.add(jobId);
    }

    synchronized public void removeJob(String jobId) {
        this.aliveJobs.remove(jobId);
        crunchJobCounters(jobId);
    }
}