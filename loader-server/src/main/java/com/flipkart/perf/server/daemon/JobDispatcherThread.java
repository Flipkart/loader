package com.flipkart.perf.server.daemon;

import com.flipkart.perf.common.util.Clock;
import com.flipkart.perf.server.domain.LoadPart;
import com.flipkart.perf.server.util.AgentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.flipkart.perf.server.cache.AgentsCache;
import com.flipkart.perf.server.domain.Job;
import com.flipkart.perf.server.domain.LoaderAgent;
import com.flipkart.perf.server.domain.PerformanceRun;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
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

        loadQueuedJobs();
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
                PerformanceRun performanceRun = job.performanceRun();

                if(freeAgents.size() >= performanceRun.agentsNeeded()) {

                    // Check if free agents match the tags from incoming jobs
                    boolean hasMatchingAgents = true;

                    List<LoaderAgent> matchingAgents = new ArrayList<LoaderAgent>();
                    for(LoadPart lp : performanceRun.getLoadParts()) {
                        LoaderAgent matchingAgent = null;
                        for(LoaderAgent la : freeAgents) {
                            if(matchingAgents.contains(la))
                                continue;

                            // Pickup an agent which is free and has your tags or pick up an agent which has no tags
                            if(la.getTags().containsAll(lp.getAgentTags()) || la.getTags().size() == 0) {
                                matchingAgent = la;
                                matchingAgents.add(matchingAgent);
                            }
                        }
                        if(matchingAgent == null) {
                            hasMatchingAgents = false;
                            break;
                        }
                    }

                    if(hasMatchingAgents) {
                        logger.info("Checking if selected free agents are reachable or not");
                        for(LoaderAgent matchingAgent : matchingAgents){
                            AgentHelper.refreshAgentInfo(matchingAgent);
                            if(!matchingAgent.getStatus().equals(LoaderAgent.LoaderAgentStatus.FREE)) {
                                logger.warn("Agent " + matchingAgent.getIp() + " was found to be free but is in " + matchingAgent.getStatus() + " state");
                                break;
                            }
                        }

                        job = jobRequestQueue.remove();
                        job.start(matchingAgents);
                    }
                }
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        logger.info("Job Dispatcher Thread Ended");
    }

    private void loadQueuedJobs()  {
        try {
            jobRequestQueue.addAll(Job.searchJobs("", "", Arrays.asList(new String[]{"QUEUED"})));
        } catch (IOException e) {
            logger.error("Failed To Load Queued Jobs", e);
        } catch (ExecutionException e) {
            logger.error("Failed To Load Queued Jobs", e);
        }
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

    public void addJobRequest(Job job) throws IOException {
        synchronized (jobRequestQueue) {
            jobRequestQueue.add(job);
            job.queued();
        }
    }

    public void removeJobRequest(Job job) throws InterruptedException, ExecutionException, IOException {
        synchronized (jobRequestQueue) {
            jobRequestQueue.add(job);
            job.killed();
        }
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
