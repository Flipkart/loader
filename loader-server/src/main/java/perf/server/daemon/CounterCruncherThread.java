package perf.server.daemon;

import com.open.perf.util.Clock;
import com.open.perf.util.FileHelper;
import perf.server.config.JobFSConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CounterCruncherThread extends Thread {

    private final int checkInterval;
    private final JobFSConfig jobFSConfig;
    private boolean stop = false;
    private List<String> aliveJobs;
    private static CounterCruncherThread thread;
    private Map<String,List<String>> fileCachedContentMap; // Cached Content per counter throughput file
    private Map<String,FileTouchPoint> fileTouchPointMap;
    private Map<String,LastCrunchPoint> fileLastCrunchPointMap;

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
        List<File> counterFiles = new ArrayList<File>();
        for(File jobFile : jobFiles) {
            if(jobFile.getAbsolutePath().contains("counter")
                    && !jobFile.getAbsolutePath().contains("throughput")) {
                crunchJobFileCounter(jobId, jobFile);
            }
        }
    }

    private void crunchJobFileCounter(String jobId, File jobFile) {

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

}