package perf.agent.job;

import org.apache.log4j.Logger;
import perf.agent.config.JobProcessorConfig;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 30/12/12
 * Time: 7:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobProcesser extends Thread{

    private Map<String, JobRunner> jobRunners;
    private Queue<JobInfo> pendingJobs;
    private static Logger log = Logger.getLogger(JobProcesser.class);
    private JobProcessorConfig config;
    private static JobProcesser jobProcessor;

    private JobProcesser(JobProcessorConfig jobProcessorConfig) {
        this.config = jobProcessorConfig;
        this.jobRunners = new HashMap<String, JobRunner>();
        this.pendingJobs = new LinkedBlockingDeque<JobInfo>();
        start();
    }

    public static JobProcesser initialize(JobProcessorConfig jobProcessorConfig) {
        if(jobProcessor == null)
            jobProcessor = new JobProcesser(jobProcessorConfig);
        return jobProcessor;
    }

    public static JobProcesser getInstance() {
        return jobProcessor;
    }

    /**
     * 1) Add Job To pending Queue
     * 2) In Loop, thread keep checking running jobs against how many max jobs can be executed
     *  2.1)   That means this guy maintains the status Jobrunners
     * 3) Starts a Job Runner with a Job as soon as running job < max jobs it can run
     */

    public void run() {
        while(true) {
            clearFinishedJobs();
            triggerNewJobs();
            waitForNextIteration();
        }
    }

    private void clearFinishedJobs() {
        synchronized (jobRunners) {
            for(String jobId : jobIds()) {
                JobRunner jobRunner = jobRunners.get(jobId);

                if(!jobRunner.running()) {
                    jobRunners.remove(jobId);
                    log.debug("Clearing Completed Job With Id :"+jobId);
                    StatSyncThread.getInstance().removeJob(jobId);
                }
            }
            log.debug("Jobs Still Running :"+jobRunners.size());
        }
    }

    private Set<String> jobIds() {
        Set<String> jobIds = new HashSet<String>();
        for(String jobId : jobRunners.keySet())
            jobIds.add(jobId);
        return jobIds;

    }

    private void triggerNewJobs() {
        if(jobRunners.size() < config.getMaxJobs())
            triggerPendingJobs(config.getMaxJobs() - jobRunners.size());
    }

    private void triggerPendingJobs(int howMany) {
        synchronized (pendingJobs) {
            for(int i=1; i <= howMany; i++) {
                JobInfo jobInfo = pendingJobs.poll();
                if(jobInfo == null)
                    break;
                jobRunners.put(jobInfo.getJobId(), new JobRunner(jobInfo));
                StatSyncThread.getInstance().addJobToSync(jobInfo.getJobId());
            }
        }
        log.debug("Jobs Still Pending :"+pendingJobs.size());
    }

    private void waitForNextIteration() {
        try {
            log.info("Sleeping for "+config.getCheckInterval()+" ms before checking for completed/pending jobs");
            Thread.sleep(config.getCheckInterval());
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void jobRequest(JobInfo jobInfo) {
        synchronized (pendingJobs) {
            pendingJobs.add(jobInfo);
        }
    }

    public Map getJobs(String jobStatus) {
        Map<String,Set> jobs = new HashMap<String, Set>();
        if("RUNNING".equals(jobStatus.toUpperCase()))
                jobs.put("RUNNING", runningJobs());
        else if("PENDING".equals(jobStatus.toUpperCase()))
                jobs.put("PENDING", pendingJobs());
        else {
            jobs.put("RUNNING", runningJobs());
            jobs.put("PENDING", pendingJobs());
        }
        return jobs;
    }

    private Set<JobInfo> pendingJobs() {
        synchronized (pendingJobs) {
            Set<JobInfo> pendingJobSet = new HashSet<JobInfo>();
            for(Object pendingJob : pendingJobs)
                pendingJobSet.add((JobInfo) pendingJob);
            return pendingJobSet;
        }
    }

    private Set<JobInfo> runningJobs() {
        synchronized (jobRunners) {
            Set<JobInfo> runningJobSet = new HashSet<JobInfo>();
            for(JobRunner jobRunner : jobRunners.values())
                    runningJobSet.add(jobRunner.getJobInfo());
            return runningJobSet;
        }
    }

    public String killJob(String jobId) {
        JobRunner jobRunner = jobRunners.get(jobId);
        if(jobRunner != null) {
            jobRunner.getJobProcess().destroy();
            return "Job Killed Successfully";
        }
        return "Job Not Running";
    }
}
