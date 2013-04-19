package perf.server.daemon;

import com.open.perf.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.server.domain.JobInfo;
import perf.server.util.JobHelper;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Submits pending jobs to agents
 */
public class JobDispatcherThread extends Thread{
    private static Logger logger = LoggerFactory.getLogger(JobDispatcherThread.class);
    private LinkedBlockingQueue<JobInfo> jobRequestQueue;
    private static JobDispatcherThread thread;
    private boolean keepRunning;
    private boolean pause;
    private static int PAUSE_SLEEP_INTERVAL = 1000;
    private static int CHECK_INTERVAL = 1000;

    public JobDispatcherThread() {
        jobRequestQueue = new LinkedBlockingQueue<JobInfo>();
        this.keepRunning = true;
        this.pause = false;
    }

    public void run() {
        logger.info("Started Job Dispatcher Thread");

        while(keepRunning) {
            if(pause) {
                try {
                    Clock.sleep(PAUSE_SLEEP_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                continue;
            }

            //Peek The Run Name
            JobInfo jobInfo = jobRequestQueue.peek();
            if(jobInfo == null) {
                try {
                    Clock.sleep(CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                continue;
            }

            //Check if required number of agents are free
            if(agentsAvailable(jobInfo.getRunName())) {
                jobInfo = jobRequestQueue.remove();
                JobHelper.submitJob(jobInfo);
            }
        }
        logger.info("Job Dispatcher Thread Ended");
    }

    private boolean agentsAvailable(String runName) {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public static JobDispatcherThread initialize() {
        if(thread == null) {
            thread = new JobDispatcherThread();
        }
        return thread;
    }

    public static JobDispatcherThread instance() {
        return thread;
    }

    public void addJobRequest(JobInfo jobInfo) {
        jobRequestQueue.add(jobInfo);
    }

    public void stopIt() {
        this.keepRunning = false;
    }

    private class JobRequest {
        private String jobId, runName;

        private JobRequest(String runName, String jobId) {
            this.jobId = jobId;
            this.runName = runName;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getRunName() {
            return runName;
        }

        public void setRunName(String runName) {
            this.runName = runName;
        }
    }
}
