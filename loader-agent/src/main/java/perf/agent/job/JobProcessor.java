package perf.agent.job;

import org.apache.log4j.Logger;
import perf.agent.client.LoaderServerClient;
import perf.agent.config.JobProcessorConfig;
import perf.agent.config.ServerInfo;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 30/12/12
 * Time: 7:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobProcessor extends Thread{

    private Map<String, JobRunner> jobRunners;
    private Queue<JobInfo> pendingJobs;
    private static Logger log = Logger.getLogger(JobProcessor.class);
    private JobProcessorConfig config;
    private static JobProcessor jobProcessor;
    private final LoaderServerClient serverClient;

    private JobProcessor(JobProcessorConfig jobProcessorConfig, ServerInfo serverInfo) {
        this.config = jobProcessorConfig;
        this.jobRunners = new HashMap<String, JobRunner>();
        this.pendingJobs = new LinkedBlockingDeque<JobInfo>();
        this.serverClient = new LoaderServerClient(serverInfo.getHost(), serverInfo.getPort());
        start();
    }

    public static JobProcessor initialize(JobProcessorConfig jobProcessorConfig, ServerInfo serverInfo) {
        if(jobProcessor == null)
            jobProcessor = new JobProcessor(jobProcessorConfig, serverInfo);
        return jobProcessor;
    }

    public static JobProcessor getInstance() {
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
            try {
                clearFinishedJobs();
                triggerNewJobs();
            }
            catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            catch (ExecutionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            finally {
                waitForNextIteration();
            }
        }
    }

    private void clearFinishedJobs() throws InterruptedException, ExecutionException, IOException {
        synchronized (jobRunners) {
            for(String jobId : jobIds()) {
                JobRunner jobRunner = jobRunners.get(jobId);

                if(!jobRunner.running()) {
                    jobRunners.remove(jobId);
                    StatSyncThread.getInstance().removeJob(jobId);
                    this.serverClient.notifyJobIsOver(jobId);
                    // Make a Post Call to let loader-server know that job is over

                }
            }
            log.debug("Jobs Still Running :"+jobRunners.size());
        }
    }

    private void triggerNewJobs() {
        if(jobRunners.size() < config.getMaxJobs()) {
            int howMany = config.getMaxJobs() - jobRunners.size();

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
    }

    private void waitForNextIteration() {
        try {
            //log.debug("Sleeping for "+config.getCheckInterval()+" ms before checking for completed/pending jobs");
            Thread.sleep(config.getCheckInterval());
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    public void addJobRequest(JobInfo jobInfo) {
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

    private Set<String> jobIds() {
        Set<String> jobIds = new HashSet<String>();
        for(String jobId : jobRunners.keySet())
            jobIds.add(jobId);
        return jobIds;

    }
}
