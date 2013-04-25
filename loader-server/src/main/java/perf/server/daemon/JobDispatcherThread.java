package perf.server.daemon;

import com.open.perf.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.server.cache.AgentsCache;
import perf.server.domain.Job;
import perf.server.domain.LoaderAgent;
import perf.server.domain.PerformanceRun;
import perf.server.util.JobHelper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Submits pending jobs to agents
 */
public class JobDispatcherThread extends Thread{
    private static Logger logger = LoggerFactory.getLogger(JobDispatcherThread.class);
    private LinkedBlockingQueue<Job> jobRequestQueue;
    private static JobDispatcherThread thread;
    private boolean keepRunning;
    private boolean pause;
    private static int PAUSE_SLEEP_INTERVAL = 1000;
    private static int CHECK_INTERVAL = 2000;

    public JobDispatcherThread() {
        jobRequestQueue = new LinkedBlockingQueue<Job>();
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
            Job job = jobRequestQueue.peek();
            if(job == null) {
                try {
                    Clock.sleep(CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                continue;
            }

            //Check if required number of agents are free
            try {
                List<LoaderAgent> freeAgents = AgentsCache.freeAgents();
                PerformanceRun performanceRun = JobHelper.instance().getPerformanceRun(job.getRunName());
                if(freeAgents.size() >= performanceRun.agentsNeeded()) {
                    job = jobRequestQueue.remove();
                    JobHelper.instance().submitJob(job, freeAgents);
                }
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        logger.info("Job Dispatcher Thread Ended");
    }


    public static JobDispatcherThread initialize() {
        if(thread == null) {
            thread = new JobDispatcherThread();
            thread.start();
        }
        return thread;
    }

    public static JobDispatcherThread instance() {
        return thread;
    }

    public void addJobRequest(Job job) {
        jobRequestQueue.add(job);
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
