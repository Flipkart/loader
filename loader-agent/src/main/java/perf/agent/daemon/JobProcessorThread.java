package perf.agent.daemon;

import com.open.perf.jackson.ObjectMapperUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.agent.client.LoaderServerClient;
import perf.agent.config.JobFSConfig;
import perf.agent.config.JobProcessorConfig;
import perf.agent.job.AgentJob;
import perf.agent.job.JobRunnerThread;
import perf.agent.util.SystemInfo;

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
    private Queue<AgentJob> pendingAgentJobs;
    private static Logger logger = LoggerFactory.getLogger(JobProcessorThread.class);
    private JobProcessorConfig config;
    private static JobProcessorThread instance;
    private final LoaderServerClient serverClient;
    private final JobFSConfig jobFSConfig;
    private static final ObjectMapper objectMapper = ObjectMapperUtil.instance();

    private JobProcessorThread(JobProcessorConfig jobProcessorConfig, LoaderServerClient serverClient, JobFSConfig jobFSConfig) {
        this.config = jobProcessorConfig;
        this.jobRunners = new HashMap<String, JobRunnerThread>();
        this.pendingAgentJobs = new LinkedBlockingDeque<AgentJob>();
        this.serverClient = serverClient;
        this.jobFSConfig = jobFSConfig;
        //60% of system heap
        int maxJobHeapSize = (int)(SystemInfo.getTotalPhysicalMemorySize() /1024 / 1024 * 60 / 100);
        this.config.setJobCLIFormat(this.config.getJobCLIFormat().replace("{MAX_HEAP_MB}",""+maxJobHeapSize));
        start();
    }

    public static JobProcessorThread initialize(JobProcessorConfig jobProcessorConfig, LoaderServerClient serverClient, JobFSConfig jobFSConfig) throws IOException, InterruptedException, ExecutionException {
        if(instance == null)
            instance = new JobProcessorThread(jobProcessorConfig, serverClient, jobFSConfig);

        instance.cleanIncompleteJobs();
        return instance;
    }

    private void cleanIncompleteJobs() throws IOException, InterruptedException, ExecutionException {
        List<String> runningJobs = objectMapper.readValue(new File(jobFSConfig.getRunningJobsFile()), List.class);
        while(runningJobs.size() > 0) {
            String runningJob = runningJobs.remove(0);
            AgentJob agentJob = objectMapper.readValue(new File(jobFSConfig.getJobFile(runningJob)), AgentJob.class);
            agentJob.kill();
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
                }
            }
            logger.debug("Jobs Still Running :"+jobRunners.size());
        }
    }

    private void triggerNewJobs() {
        if(jobRunners.size() < config.getMaxJobs()) {
            int howMany = config.getMaxJobs() - jobRunners.size();

            synchronized (pendingAgentJobs) {
                for(int i=1; i <= howMany; i++) {
                    AgentJob agentJob = pendingAgentJobs.poll();
                    if(agentJob == null)
                        break;
                    jobRunners.put(agentJob.getJobId(), new JobRunnerThread(agentJob, jobFSConfig));
                }
            }
            logger.debug("Jobs Still Pending :"+ pendingAgentJobs.size());

        }
    }

    private void waitForNextIteration() {
        try {
            Thread.sleep(config.getPendingJobCheckInterval());
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void addJobRequest(AgentJob agentJob) throws IOException {
        synchronized (pendingAgentJobs) {
            pendingAgentJobs.add(agentJob);
            agentJob.queued();
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

    private Set<AgentJob> pendingJobs() {
        synchronized (pendingAgentJobs) {
            Set<AgentJob> pendingAgentJobSet = new HashSet<AgentJob>();
            for(Object pendingJob : pendingAgentJobs)
                pendingAgentJobSet.add((AgentJob) pendingJob);
            return pendingAgentJobSet;
        }
    }

    private Set<AgentJob> runningJobs() {
        synchronized (jobRunners) {
            Set<AgentJob> runningAgentJobSet = new HashSet<AgentJob>();
            for(JobRunnerThread jobRunnerThread : jobRunners.values())
                    runningAgentJobSet.add(jobRunnerThread.getAgentJob());
            return runningAgentJobSet;
        }
    }

    private Set<String> jobIds() {
        Set<String> jobIds = new HashSet<String>();
        for(String jobId : jobRunners.keySet())
            jobIds.add(jobId);
        return jobIds;

    }

    public static void main(String[] args) {
        System.out.println((int)(8328228864l/1024/1024*60/100));
    }
}
