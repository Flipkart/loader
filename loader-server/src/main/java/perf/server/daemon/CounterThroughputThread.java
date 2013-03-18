package perf.server.daemon;

import com.open.perf.constant.MathConstant;
import com.open.perf.util.Clock;
import com.open.perf.util.FileHelper;
import org.apache.log4j.Logger;
import perf.server.config.JobFSConfig;

import java.io.*;
import java.util.*;

/**
 * Calculate throughput on collected counters
 */
public class CounterThroughputThread extends Thread {

    private final int checkInterval;
    private final JobFSConfig jobFSConfig;
    private boolean stop = false;
    private List<String> aliveJobs;

    private Map<String,FileTouchPoint> fileTouchPointMap;
    private Map<String,LastPoint> fileLastCrunchPointMap;

    private static CounterThroughputThread thread;
    private static final long CLUB_CRUNCH_DURATION = 10 * MathConstant.BILLION; // Club and crunch duration to calculate throughput
    private static final long CRUNCH_DATA_OLDER_THAN = 30 * MathConstant.BILLION; // As long as job is alive crunch data which is older than 30 secs
    private static Logger logger = Logger.getLogger(CounterThroughputThread.class);
    private static final String FILE_EXTENSION = "throughput";

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

    private static class LastPoint {
        private long time;
        private long count;

        private LastPoint(long time, long count) {
            this.time = time;
            this.count = count;
        }
    }

    private CounterThroughputThread(JobFSConfig jobFSConfig, int checkInterval) {
        this.jobFSConfig = jobFSConfig;
        this.checkInterval = checkInterval;
        this.aliveJobs = new ArrayList<String>();
        this.fileTouchPointMap = new HashMap<String, FileTouchPoint>();
        this.fileLastCrunchPointMap = new HashMap<String, LastPoint>();
    }

    public static CounterThroughputThread initialize(JobFSConfig jobFSConfig, int checkInterval) {
        if(thread == null) {
            thread = new CounterThroughputThread(jobFSConfig, checkInterval);
        }
        return thread;
    }

    public static CounterThroughputThread getCounterCruncherThread() {
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
            if(jobFile.getAbsolutePath().contains("cumulative")) {
                crunchJobFileCounter(jobId, jobFile);
            }
        }
    }

    public void crunchJobFileCounter(String jobId, File jobFile) {
        logger.info("Crunching File :"+jobFile.getAbsolutePath() + " for job id :" + jobId);
        List<String> fileContentLines = readFileContentAsList(jobFile);

        if(fileContentLines.size() > 0) {
            String newFile = jobFile.getAbsolutePath().replace("cumulative", FILE_EXTENSION);
            BufferedWriter bw = null;
            try {
                bw = FileHelper.bufferedWriter(newFile, true);

                LastPoint lastPoint = this.fileLastCrunchPointMap.get(jobFile.getAbsolutePath());
                if(lastPoint == null) {
                    String firstContentLine = fileContentLines.remove(0);
                    String[] tokens = firstContentLine.split(",");
                    lastPoint = new LastPoint(Long.parseLong(tokens[0]), Long.parseLong(tokens[1]));
                }

                while(fileContentLines.size() > 0) {
                    String currentContentLine = fileContentLines.remove(0);
                    String[] tokens = currentContentLine.split(",");
                    long currentContentTime = Long.parseLong(tokens[0]);
                    long currentContentCount = Long.parseLong(tokens[1]);
                    long opsDone = currentContentCount - lastPoint.count;
                    long timeTakenNS = currentContentTime - lastPoint.time;
                    float timeTakenSec = (float)timeTakenNS / MathConstant.BILLION;
                    float tps = opsDone/timeTakenSec;

                    String tpsLine = currentContentTime+ "," + tps;
                    bw.write(tpsLine + "\n");
                    bw.flush();

                    lastPoint = new LastPoint(currentContentTime, currentContentCount);
                    this.fileLastCrunchPointMap.put(jobFile.getAbsolutePath(), lastPoint);
                }

            } catch (FileNotFoundException e) {
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
        CounterThroughputThread t = new CounterThroughputThread(null, 10);
        t.crunchJobFileCounter("", new File("/var/log/loader-server/jobs/a09cc4f7-e868-4b2c-ae55-d9e992c2bd46/jobStats/SampleGroup/counters/tmp1.cumulative"));

    }
}