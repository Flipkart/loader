package perf.agent.daemon;

import com.open.perf.jackson.ObjectMapperUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.agent.client.LoaderServerClient;
import perf.agent.config.JobFSConfig;
import perf.agent.config.JobProcessorConfig;
import perf.agent.job.Job;
import perf.agent.job.JobRunnerThread;
import perf.agent.util.ResponseBuilder;

import javax.ws.rs.WebApplicationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * This thread processes the incoming job requests.
 */
public class JobProcessorThread extends Thread{

    private Map<String, JobRunnerThread> jobRunners;
    private Queue<Job> pendingJobs;
    private static Logger logger = LoggerFactory.getLogger(JobProcessorThread.class);
    private JobProcessorConfig config;
    private static JobProcessorThread instance;
    private final LoaderServerClient serverClient;
    private final JobFSConfig jobFSConfig;
    private static final ObjectMapper objectMapper = ObjectMapperUtil.instance();

    private JobProcessorThread(JobProcessorConfig jobProcessorConfig, LoaderServerClient serverClient, JobFSConfig jobFSConfig) {
        this.config = jobProcessorConfig;
        this.jobRunners = new HashMap<String, JobRunnerThread>();
        this.pendingJobs = new LinkedBlockingDeque<Job>();
        this.serverClient = serverClient;
        this.jobFSConfig = jobFSConfig;
        start();
    }

    public static JobProcessorThread initialize(JobProcessorConfig jobProcessorConfig, LoaderServerClient serverClient, JobFSConfig jobFSConfig) throws IOException, InterruptedException {
        if(instance == null)
            instance = new JobProcessorThread(jobProcessorConfig, serverClient, jobFSConfig);

        instance.cleanIncompleteJobs();
        return instance;
    }

    private void cleanIncompleteJobs() throws IOException, InterruptedException {
        List<String> runningJobs = objectMapper.readValue(new File(jobFSConfig.getRunningJobsFile()), List.class);
        while(runningJobs.size() > 0) {
            String runningJob = runningJobs.remove(0);
            Job job = objectMapper.readValue(new File(jobFSConfig.getJobFile(runningJob)), Job.class);
            job.kill();
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(jobFSConfig.getRunningJobsFile()), runningJobs);
    }

    public static JobProcessorThread instance() {
        return instance;
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
                    JobStatsSyncThread.instance().removeJob(jobId);
                    JobHealthCheckThread.instance().removeJob(jobRunnerThread.getJob());
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
                    Job job = pendingJobs.poll();
                    if(job == null)
                        break;
                    jobRunners.put(job.getJobId(), new JobRunnerThread(job, jobFSConfig));
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

    public void killJob(String jobId) throws IOException, InterruptedException {
        JobRunnerThread jobRunnerThread = jobRunners.get(jobId);
        if(jobRunnerThread != null) {
            Job job = jobRunnerThread.getJob();
            job.kill();
        }
        throw new WebApplicationException(ResponseBuilder.jobNotFound(jobId));
    }

    public void addJobRequest(Job job) throws IOException {
        synchronized (pendingJobs) {
            pendingJobs.add(job);
            job.queued();
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

    private Set<Job> pendingJobs() {
        synchronized (pendingJobs) {
            Set<Job> pendingJobSet = new HashSet<Job>();
            for(Object pendingJob : pendingJobs)
                pendingJobSet.add((Job) pendingJob);
            return pendingJobSet;
        }
    }

    private Set<Job> runningJobs() {
        synchronized (jobRunners) {
            Set<Job> runningJobSet = new HashSet<Job>();
            for(JobRunnerThread jobRunnerThread : jobRunners.values())
                    runningJobSet.add(jobRunnerThread.getJob());
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
