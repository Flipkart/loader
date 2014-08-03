package com.flipkart.perf.server.daemon;

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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Submits pending jobs to agents
 */
public class JobDispatcherThread extends Thread{
    private static Logger logger = LoggerFactory.getLogger(JobDispatcherThread.class);
    private LinkedBlockingQueue<Job> jobRequestQueue;
    private static JobDispatcherThread thread;

    public JobDispatcherThread() {
        jobRequestQueue = new LinkedBlockingQueue<Job>();
        loadQueuedJobs();
    }

    public void run() {
        try {
            Job job = jobRequestQueue.peek();
            if(job == null) {
                return;
            }
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
        } catch (Exception e) {
            logger.error("", e);
        }
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

    public static JobDispatcherThread initialize(ScheduledExecutorService scheduledExecutorService, int interval) {
        if(thread == null) {
        	synchronized(JobDispatcherThread.class) {
        		thread = new JobDispatcherThread();
        		thread.start();
        		scheduledExecutorService.scheduleWithFixedDelay(thread,
        				1000,
        				interval,
        				TimeUnit.MILLISECONDS);
        	}
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
            jobRequestQueue.remove(job);
            job.killed();
        }
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
