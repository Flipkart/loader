package perf.server.util;

import com.open.perf.domain.Load;
import com.open.perf.jackson.ObjectMapperUtil;
import com.open.perf.util.FileHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.server.client.LoaderAgentClient;
import perf.server.client.MonitoringClient;
import perf.server.config.AgentConfig;
import perf.server.config.JobFSConfig;
import perf.server.config.MonitoringAgentConfig;
import perf.server.daemon.CounterCompoundThread;
import perf.server.daemon.CounterThroughputThread;
import perf.server.daemon.TimerComputationThread;
import perf.server.domain.*;
import perf.server.exception.JobException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 18/4/13
 * Time: 4:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobHelper {
    private JobFSConfig jobFSConfig;
    private AgentConfig agentConfig;
    private MonitoringAgentConfig monitoringAgentConfig;

    private static JobHelper myInstance;
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();
    private static Logger log = LoggerFactory.getLogger(JobHelper.class);


    public JobHelper(JobFSConfig jobFSConfig, AgentConfig agentConfig, MonitoringAgentConfig monitoringAgentConfig) {
        this.jobFSConfig = jobFSConfig;
        this.agentConfig = agentConfig;
        this.monitoringAgentConfig = monitoringAgentConfig;
    }

    public static JobHelper initialize(JobFSConfig jobFSConfig, AgentConfig agentConfig, MonitoringAgentConfig monitoringAgentConfig) {
        if(myInstance == null)
            myInstance = new JobHelper(jobFSConfig, agentConfig, monitoringAgentConfig);
        return myInstance;
    }

    public static JobHelper instance() {
        return myInstance;
    }

    public void submitJob(JobInfo jobInfo) {
        String runFile = jobFSConfig.getRunFile(jobInfo.getRunName());
        try {
            PerformanceRun performanceRun = objectMapper.readValue(new File(runFile) , PerformanceRun.class);
            jobInfo.setRunName(performanceRun.getRunName());

/*
            // Persisting Job Info(mostly status) in memory
            jobIdInfoMap.put(jobInfo.getJobId(), jobInfo);

*/
            // Persisting Job Json in Local File system.
            persistJob(jobInfo.getJobId(), performanceRun);

            // Raising request to monitoring agents to start collecting metrics from on demand resource collectors
            raiseOnDemandResourceRequest(jobInfo, performanceRun.getOnDemandMetricCollections());

            // Raising request to monitoring agents to start publishing collected metrics to Loader server
            raiseMetricPublishRequest(jobInfo, performanceRun.getMetricCollections());

            // Deploy Libraries on Agents
            deployLibrariesOnAgents(performanceRun.getLoadParts());

            // Submitting Jobs to Loader Agent
            submitJobToAgents(jobInfo, performanceRun.getLoadParts());

            CounterCompoundThread.getCounterCruncherThread().addJob(jobInfo.getJobId());
            CounterThroughputThread.getCounterCruncherThread().addJob(jobInfo.getJobId());
            TimerComputationThread.getComputationThread().addJob(jobInfo.getJobId());

            objectMapper.writeValue(new FileOutputStream(jobFSConfig.getJobStatusFile(jobInfo.getJobId())), jobInfo);
        }
        catch (Exception e) {
            log.error("Job Submission Failed",e);
            jobInfo.setJobStatus(JobInfo.JOB_STATUS.FAILED_TO_START);
            try {
                jobCleanUpOnFailure(jobInfo);
            } catch (Exception e1) {
                log.error("Job Clean up Failure",e);
            }

            throw new WebApplicationException(ResponseBuilder.response(Response.Status.INTERNAL_SERVER_ERROR, e));
        }

    }

    /**
     * Clean up Running Job In case Something went wrong while job was being submitted
     * @param jobInfo
     */
    private void jobCleanUpOnFailure(JobInfo jobInfo) throws InterruptedException, ExecutionException, JobException, IOException {
        killJobInAgents(jobInfo.getJobId(), jobInfo.getAgentsJobStatus().keySet());
        stopMonitoring(jobInfo.getJobId());
    }

    /**
     * Persist Job Details in FS
     * @param jobId
     * @param performanceRun
     * @throws java.io.IOException
     */
    private void persistJob(String jobId, PerformanceRun performanceRun) throws IOException {
        String runName = performanceRun.getRunName();

        // Add file containing run name in job folder
        String jobRunNameFile = jobFSConfig.getJobRunNameFile(jobId);
        FileHelper.createFilePath(jobRunNameFile);
        FileHelper.persistStream(new ByteArrayInputStream(runName.getBytes()),
                jobRunNameFile,
                false);

        // Adding job ids in run folder file
        String runJobsFile = jobFSConfig.getRunJobsFile(runName);
        FileHelper.createFilePath(runJobsFile);
        FileHelper.persistStream(new ByteArrayInputStream((jobId + "\n").getBytes()),
                runJobsFile,
                true);
    }

    /**
     * Raise On Demand Resource Request to Monitoring Agent as Part of Load Job
     * @param jobInfo
     * @param onDemandMetricCollections
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     * @throws IOException
     */
    private void raiseOnDemandResourceRequest(JobInfo jobInfo, List<OnDemandMetricCollection> onDemandMetricCollections) throws InterruptedException, ExecutionException, IOException {
        for(OnDemandMetricCollection onDemandMetricCollection : onDemandMetricCollections) {
            String agentIp = onDemandMetricCollection.getAgent();
            new MonitoringClient(agentIp,
                    monitoringAgentConfig.getAgentPort()).
                    raiseOnDemandResourceRequest(onDemandMetricCollection.buildRequest(jobInfo.getJobId()));
            jobInfo.addMonitoringAgent(agentIp);
            log.info("Request "+onDemandMetricCollection+" raised on Agent "+agentIp);
        }
    }

    /**
     * Raise Metric Publish request to Monitoring Agent
     * @param jobInfo
     * @param metricCollections
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    private void raiseMetricPublishRequest(JobInfo jobInfo, List<MetricCollection> metricCollections) throws InterruptedException, ExecutionException, IOException {
        for(MetricCollection metricCollection : metricCollections) {
            String agentIp = metricCollection.getAgent();
            new MonitoringClient(agentIp,
                    monitoringAgentConfig.getAgentPort()).
                    raiseMetricPublishRequest(metricCollection.buildRequest(jobInfo.getJobId()));
            jobInfo.addMonitoringAgent(agentIp);
            log.info("Request "+metricCollection+" raised on Agent "+agentIp);
        }
    }

    /**
     * Submitting Load Job To Loader Agents
     * @param jobInfo
     * @param loadParts
     * @throws IOException
     * @throws perf.server.exception.JobException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void submitJobToAgents(JobInfo jobInfo, List<LoadPart> loadParts)
            throws IOException, JobException, ExecutionException, InterruptedException {
        jobInfo.setStartTime(new Date());
        for(LoadPart loadPart : loadParts) {
            // Submitting Job To Agent
            List<String> agentIps = loadPart.getAgents();
            List<String> classes = loadPart.getClasses();

            StringBuilder classListWithNewLine = new StringBuilder();
            for(String clazz : classes)
                classListWithNewLine.append(clazz+"\n");

            for(String agentIp : agentIps) {
                submitJobToAgent(agentIp,
                        jobInfo.getJobId(),
                        loadPart.getLoad(),
                        classListWithNewLine.toString());
                jobInfo.jobRunningInAgent(agentIp);
            }
        }
    }

    /**
     * Deploy Platform Libs and Class Libs on Loader agents If Required
     * @param loadParts
     * @throws IOException
     * @throws JobException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void deployLibrariesOnAgents(List<LoadPart> loadParts)
            throws IOException, JobException, ExecutionException, InterruptedException {
        for(LoadPart loadPart : loadParts) {
            // Submitting Job To Agent
            List<String> agentIps = loadPart.getAgents();
            List<String> classes = loadPart.getClasses();

            StringBuilder classListWithNewLine = new StringBuilder();
            for(String clazz : classes)
                classListWithNewLine.append(clazz+"\n");

            for(String agentIp : agentIps) {
                DeploymentHelper.instance().deployPlatformLibsOnAgent(agentIp);
                DeploymentHelper.instance().deployClassLibsOnAgent(agentIp, classListWithNewLine.toString().trim());
            }
        }
    }

    /**
     * Submitting Job To Loader Agent
     * @param agentIp
     * @param jobId
     * @param load
     * @throws IOException
     * @throws JobException
     */
    private void submitJobToAgent(String agentIp, String jobId, Load load, String classListStr)
            throws InterruptedException, ExecutionException, JobException, IOException {
        log.info("Agent Ip :" + agentIp);
        new LoaderAgentClient(agentIp,
                agentConfig.getAgentPort()).
                submitJob(jobId, load, classListStr);
        log.info("Load Job " + load + " submitted to Agent " + agentIp);
    }

    public void killJobInAgents(String jobId, Collection<String> agents)
            throws InterruptedException, ExecutionException, JobException, IOException {
        if(!jobInfo.getJobStatus().equals(JobInfo.JOB_STATUS.COMPLETED) &&
                !jobInfo.getJobStatus().equals(JobInfo.JOB_STATUS.KILLED)) {

            Map<String, JobInfo.JOB_STATUS> agentsJobStatusMap = jobInfo.getAgentsJobStatus();
            for(String agent : agents) {
                if(!agentsJobStatusMap.get(agent).equals(JobInfo.JOB_STATUS.KILLED) &&
                        !agentsJobStatusMap.get(agent).equals(JobInfo.JOB_STATUS.COMPLETED)) {
                    new LoaderAgentClient(agent, agentConfig.getAgentPort()).killJob(jobId);
                    jobInfo.jobKilledInAgent(agent);
                }
            }
        }
        objectMapper.writeValue(new FileOutputStream(jobFSConfig.getJobStatusFile(jobId)), jobInfo);
    }

    /**
     * Stop monitoring in all agents for the job
     * @param jobId
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void stopMonitoring(String jobId, Set<String> monitoringAgentIps) throws IOException, ExecutionException, InterruptedException {
        jobLastResourceMetricInstanceMap.remove(jobId);

        // Need to get All
        Set<String> monitoringAgentIps = jobIdInfoMap.get(jobId).getMonitoringAgents();

        for(String agentIp : monitoringAgentIps) {
            MonitoringClient monitoringAgent = new MonitoringClient(agentIp, monitoringAgentConfig.getAgentPort());
            monitoringAgent.deleteOnDemandResourceRequest(jobId);
            monitoringAgent.deletePublishResourceRequest(jobId);
        }
    }


}