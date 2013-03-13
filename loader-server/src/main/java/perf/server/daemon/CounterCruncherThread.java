package perf.server.daemon;

import com.open.perf.constant.MathConstant;
import com.open.perf.util.Clock;
import com.open.perf.util.FileHelper;
import perf.server.config.JobFSConfig;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class CounterCruncherThread extends Thread {

    private final int checkInterval;
    private final JobFSConfig jobFSConfig;
    private boolean stop = false;
    private List<String> aliveJobs;

    private Map<String,List<String>> fileCachedContentMap; // Cached Content per counter throughput file
    private Map<String,FileTouchPoint> fileTouchPointMap;
    private Map<String,LastCrunchPoint> fileLastCrunchPointMap;

    private static CounterCruncherThread thread;
    private static final long CLUB_CRUNCH_DURATION = 10 * MathConstant.BILLION; // Club and crunch duration to calculate throughput
    private static final long CRUNCH_DATA_OLDER_THAN = 30 * MathConstant.BILLION; // As long as job is alive crunch data which is older than 30 secs


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

    private CounterCruncherThread(JobFSConfig jobFSConfig, int checkInterval) {
        this.jobFSConfig = jobFSConfig;
        this.checkInterval = checkInterval;
        this.aliveJobs = new ArrayList<String>();
        this.fileCachedContentMap = new HashMap<String, List<String>>();
        this.fileTouchPointMap = new HashMap<String, FileTouchPoint>();
        this.fileLastCrunchPointMap = new HashMap<String, LastCrunchPoint>();
    }

    public static CounterCruncherThread initialize(JobFSConfig jobFSConfig, int checkInterval) {
        if(thread == null) {
            thread = new CounterCruncherThread(jobFSConfig, checkInterval);
        }
        return thread;
    }

    public static CounterCruncherThread getCounterCruncherThread() {
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
        List<File> jobFiles = FileHelper.pathFiles(this.jobFSConfig.getJobPath(jobId), true);
        for(File jobFile : jobFiles) {
            if(jobFile.getAbsolutePath().contains("counter")
                    && !jobFile.getAbsolutePath().contains("throughput")) {
                crunchJobFileCounter(jobId, jobFile);
            }
        }
    }

    private void crunchJobFileCounter(String jobId, File jobFile) {
        List<String> fileContentLines = readFileContentAsList(jobFile);
        if(fileContentLines.size() > 0) {
            List<String> cachedContent = this.fileCachedContentMap.get(jobFile.getAbsolutePath());
            if(cachedContent == null) {
                cachedContent = new ArrayList<String>();
            }
            cachedContent.addAll(fileContentLines);
            Collections.sort(cachedContent);

            String throughputFile = jobFile.getAbsolutePath() + ".throughput";
            BufferedWriter bw = null;
            try {
                bw = FileHelper.bufferedWriter(throughputFile, true);
                long firstEntryTime = Long.parseLong(cachedContent.get(0).split(",")[0]);
                long lastEntryTime = Long.parseLong(cachedContent.get(cachedContent.size()-1).split(",")[0]);
                if(lastEntryTime - firstEntryTime > CRUNCH_DATA_OLDER_THAN
                        || jobOver(jobId)) {

                    LastCrunchPoint lastCrunchPoint = this.fileLastCrunchPointMap.get(jobFile.getAbsolutePath());
                    if(lastCrunchPoint == null) {
                        String firstContentLine = cachedContent.remove(0);
                        String[] tokens = firstContentLine.split(",");
                        lastCrunchPoint = new LastCrunchPoint(Long.parseLong(tokens[0]),
                                Long.parseLong(tokens[1]));
                        bw.write(tokens[0] + ",0.0,0\n");
                        bw.flush();
                    }

                    long opsDone = 0;
                    while(cachedContent.size() > 0) {
                        String cachedContentLine = cachedContent.remove(0);
                        String[] tokens = cachedContentLine.split(",");
                        long currentContentTime = Long.parseLong(tokens[0]);
                        long currentContentCount = Long.parseLong(tokens[1]);

                        // Collect Content To Crunch
                            opsDone += currentContentCount;

                        // Next Content Time
                        long nextContentTime = -1;
                        if(cachedContent.size() > 0) {
                            nextContentTime = Long.parseLong(cachedContent.get(0).split(",")[0]);
                        }

                        // Crunch if collected data for 10 seconds have been collected and next Content Time is different
                        if(currentContentTime - lastCrunchPoint.time > CLUB_CRUNCH_DURATION
                                && (nextContentTime == -1 || currentContentTime != nextContentTime )) {
                            // Have got data, crunch them
                            long timeTakenNS = currentContentTime - lastCrunchPoint.time;
                            float timeTakenSec = (float)timeTakenNS / MathConstant.BILLION;
                            float tps = opsDone/timeTakenSec;

                            long totalOpsDoneSoFar = lastCrunchPoint.countSoFar + opsDone;
                            lastCrunchPoint = new LastCrunchPoint(currentContentTime, totalOpsDoneSoFar);
                            this.fileLastCrunchPointMap.put(jobFile.getAbsolutePath(), lastCrunchPoint);

                            String crunchedStatsLine = lastCrunchPoint.time + "," + tps + "," + lastCrunchPoint.countSoFar;
                            bw.write(crunchedStatsLine + "\n");
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
        CounterCruncherThread t = new CounterCruncherThread(null, 10);
        t.crunchJobFileCounter("", new File("/var/log/loader-server/jobs/66893a74-86f4-4ec0-bff3-55213b83cf35/agents/127.0.0.1/jobStats/SampleGroup/counters/DummyFunction_count"));

    }
}