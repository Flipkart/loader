package perf.agent.daemon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.agent.client.LoaderServerClient;
import perf.agent.config.JobFSConfig;
import perf.agent.config.JobProcessorConfig;
import perf.agent.job.JobInfo;
import perf.agent.job.JobRunnerThread;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * This thread processes the incoming job requests.
 */
public class JobProcessorThread extends Thread{

    private Map<String, JobRunnerThread> jobRunners;
    private Queue<JobInfo> pendingJobs;
    private static Logger logger = LoggerFactory.getLogger(JobProcessorThread.class);
    private JobProcessorConfig config;
    private static JobProcessorThread jobProcessorThread;
    private final LoaderServerClient serverClient;
    private final JobFSConfig jobFSConfig;

    private JobProcessorThread(JobProcessorConfig jobProcessorConfig, LoaderServerClient serverClient, JobFSConfig jobFSConfig) {
        this.config = jobProcessorConfig;
        this.jobRunners = new HashMap<String, JobRunnerThread>();
        this.pendingJobs = new LinkedBlockingDeque<JobInfo>();
        this.serverClient = serverClient;
        this.jobFSConfig = jobFSConfig;
        start();
    }

    public static JobProcessorThread initialize(JobProcessorConfig jobProcessorConfig, LoaderServerClient serverClient, JobFSConfig jobFSConfig) {
        if(jobProcessorThread == null)
            jobProcessorThread = new JobProcessorThread(jobProcessorConfig, serverClient, jobFSConfig);
        return jobProcessorThread;
    }

    public static JobProcessorThread getInstance() {
        return jobProcessorThread;
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
                JobRunnerThread jobRunnerThread = jobRunners.get(jobId);

                if(!jobRunnerThread.running()) {
                    jobRunners.remove(jobId);
                    JobStatsSyncThread.getInstance().removeJob(jobId);
                    JobHealthCheckThread.instance().remove(jobRunnerThread.getJobInfo());
                    this.serverClient.notifyJobIsOver(jobId);
                    // Make a Post Call to let loader-server know that job is over

                }
            }
            logger.debug("Jobs Still Running :"+jobRunners.size());
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
                    jobRunners.put(jobInfo.getJobId(), new JobRunnerThread(jobInfo, jobFSConfig));
                    JobStatsSyncThread.getInstance().addJobToSync(jobInfo.getJobId());
                    JobHealthCheckThread.instance().add(jobInfo);
                }
            }
            logger.debug("Jobs Still Pending :"+pendingJobs.size());

        }
    }

    private void waitForNextIteration() {
        try {
            Thread.sleep(config.getPendingJobCheckInterval());
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String killJob(String jobId) throws IOException, InterruptedException {
        JobRunnerThread jobRunnerThread = jobRunners.get(jobId);
        if(jobRunnerThread != null) {
            //jobRunnerThread.getJobProcess().destroy();
            Runtime.getRuntime().exec(new String[]{"/bin/sh","-c","kill -9 `ps aux | grep "+jobId+" | grep -v grep | tr -s \" \" \":\" |cut -f 2 -d \":\"`"}).
                    waitFor();
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
            for(JobRunnerThread jobRunnerThread : jobRunners.values())
                    runningJobSet.add(jobRunnerThread.getJobInfo());
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
