package perf.server.resource;

import com.open.perf.util.FileHelper;
import com.yammer.dropwizard.jersey.params.BooleanParam;
import com.yammer.metrics.annotation.Timed;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import perf.server.cache.AgentsCache;
import perf.server.config.AgentConfig;
import perf.server.config.JobFSConfig;
import perf.server.config.MonitoringAgentConfig;
import perf.server.daemon.CounterCompoundThread;
import perf.server.daemon.CounterThroughputThread;
import perf.server.daemon.JobDispatcherThread;
import perf.server.daemon.TimerComputationThread;
import perf.server.domain.Job;
import perf.server.domain.JobRequest;
import perf.server.domain.ResourceCollectionInstance;
import perf.server.exception.JobException;
import perf.server.util.JobHelper;
import perf.server.util.ResponseBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.codehaus.jackson.JsonParser.Feature;

/**
 * Resource that receive Performance Job Request from Client Lib or Loader-Server UI
 */
@Path("/jobs")
public class JobResource {

    private final MonitoringAgentConfig monitoringAgentConfig;
    private AgentConfig agentConfig;
    private JobFSConfig jobFSConfig;
    private static JobHelper jobHelper;
    private static ObjectMapper objectMapper;
    private static Map<String,Map<String,ResourceCollectionInstance>> jobLastResourceMetricInstanceMap;
    private static Logger log;

    static {
        objectMapper = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("MMM dd hh:mm:ss z yyyy");
        objectMapper.setDateFormat(dateFormat);
        objectMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        jobLastResourceMetricInstanceMap = new HashMap<String, Map<String, ResourceCollectionInstance>>();
        jobHelper = JobHelper.instance();
        log = Logger.getLogger(JobResource.class);
    }

    public JobResource(AgentConfig agentConfig,
                       MonitoringAgentConfig monitoringAgentConfig,
                       JobFSConfig jobFSConfig) {
        this.agentConfig = agentConfig;
        this.monitoringAgentConfig = monitoringAgentConfig;
        this.jobFSConfig = jobFSConfig;
    }
    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl -X POST -d @file-containing-runName http://localhost:9999/loader-server/jobs --header "Content-Type:application/json"
     {"runName" : "runName"}
     It simply puts the performance job request in Job Dispatcher Thread Queue.
     * @param jobRequest
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Timed
    public Job submitJob(JobRequest jobRequest) {
        return raiseJobRequest(jobRequest);
    }

    private Job raiseJobRequest(JobRequest jobRequest) {
        runExistsOrException(jobRequest.getRunName());
        Job job = new Job().
                setJobId(UUID.randomUUID().toString()).
                setRunName(jobRequest.getRunName());

        JobDispatcherThread.instance().addJobRequest(job);
        return job;
    }


    /**
     * Submit the same job again if existing job has finished.
     * @param oldJobId
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws JobException
     */
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{jobId}/rerun")
    @POST
    @Timed
    public Job rerunJob(@PathParam("jobId") String oldJobId)
            throws IOException, ExecutionException, InterruptedException, JobException {

        Job oldJob = jobHelper.jobExistsOrException(oldJobId);
        if(!oldJob.completed())
            throw new WebApplicationException(ResponseBuilder.jobNotOver(oldJobId));

        return raiseJobRequest(new JobRequest().setRunName(oldJob.getRunName()));
    }

    /**
     * Get Job Details
     * @param jobId
     * @return
     * @throws IOException
     */
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{jobId}")
    @GET
    @Timed
    public Job getJob(@PathParam("jobId") String jobId) throws IOException, ExecutionException {
        return jobHelper.jobExistsOrException(jobId);
    }

    /**
     * Search Job based on runName, jobId and job status
     * By default it would search all running jobs only (And its little slow)
     * @param searchRunName
     * @param searchJobId
     * @param searchJobStatus
     * @return
     */
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    public List<Job> getJobs(@QueryParam("runName") @DefaultValue("") String searchRunName,
                                        @QueryParam("jobId") @DefaultValue("") String searchJobId,
                                        @QueryParam("jobStatus") @DefaultValue("RUNNING")String searchJobStatus) throws IOException {
        return jobHelper.searchJobs(searchJobId, searchRunName, searchJobStatus);
    }

    /**
     * Agents Publish Job Load stats on this resource
     * @param request
     * @param jobId
     * @param relatedFilePath
     * @param jobStatsStream
     * @throws IOException
     * @throws InterruptedException
     */
    @Path("/{jobId}/jobStats")
    @POST
    @Timed
    synchronized public void jobStats(@Context HttpServletRequest request,
                                      @PathParam("jobId") String jobId,
                                      @QueryParam("file") String relatedFilePath,
                                      InputStream jobStatsStream)
            throws IOException, InterruptedException {
        jobHelper.persistJobStatsComingFromAgent(jobId, request.getRemoteAddr(), relatedFilePath, jobStatsStream);
    }

    /**
     * Returns Job Stats Meta Data. Useful to see what all stats are being collected as part of performance testing
     * @param jobId
     * @return
     */
    @Path("/{jobId}/jobStats")
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public List<JobHelper.GroupStatsMeta> getJobMetricsStatsMeta(@PathParam("jobId") String jobId) throws IOException, ExecutionException {
        jobHelper.jobExistsOrException(jobId);
        return jobHelper.getJobMetricsStatsMeta(jobId);
    }

    /**
     * Returns particular function stats
     * Example /jobId/jobStats/groups/sampleGroup/timers/timer1/agents/127.0.0.1
     * Example /jobId/jobStats/groups/sampleGroup/counters/counter1/agents/127.0.0.1
     * @param jobId
     * @return
     */
    @Path("/{jobId}/jobStats/groups/{groupName}/{metricType}/{metricName}/agents/{agent}")
    @GET
    @Timed
    @Produces(MediaType.TEXT_PLAIN)
    public InputStream getJobMetricStats(@PathParam("jobId") String jobId,
                                                    @PathParam("groupName") String groupName,
                                                    @PathParam("metricType") String metricType,
                                                    @PathParam("metricName") String metricName,
                                                    @PathParam("agent") String agent,
                                                    @QueryParam("last") @DefaultValue("false") BooleanParam last) throws IOException, ExecutionException {
        jobHelper.jobExistsOrException(jobId);
        return jobHelper.getJobMetricStats(jobId, groupName, metricType, metricName, agent, last.get());
    }

    /**
     * Monitoring Agents publishes job Related Monitoring stats here
     * @param request
     * @param jobId
     * @param resourcesCollectionInstances
     * @throws IOException
     * @throws InterruptedException
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{jobId}/monitoringStats")
    @POST
    @Timed
    public void jobMonitoringStats(@Context HttpServletRequest request,
                                      @PathParam("jobId") String jobId,
                                      Map<String, List<ResourceCollectionInstance>> resourcesCollectionInstances)
            throws IOException, InterruptedException, ExecutionException {
        jobHelper.jobExistsOrException(jobId);

        Map<String,ResourceCollectionInstance> resourcesLastInstance = jobLastResourceMetricInstanceMap.get(jobId);

        if(resourcesLastInstance == null)
            resourcesLastInstance = new HashMap<String, ResourceCollectionInstance>();

        for(String resource : resourcesCollectionInstances.keySet()) {
            String jobMonitoringStatsPath = jobFSConfig.getJobResourceMonitoringFile(jobId,
                    request.getRemoteAddr(),
                    resource);

            List<ResourceCollectionInstance> resourceCollectionInstances = resourcesCollectionInstances.get(resource);
            FileHelper.createFilePath(jobMonitoringStatsPath);

            // Get Last Persisted Metric Instance. Compare it with new one, if changed then persist
            ResourceCollectionInstance resourceLastInstance = resourcesLastInstance.get(resource);

            for(ResourceCollectionInstance resourceCollectionInstance : resourceCollectionInstances) {
                boolean persistStat = true;

                if(resourceLastInstance != null) {
                    persistStat = !resourceLastInstance.toString().equals(resourceCollectionInstance.toString());
                }

                if(persistStat) {
                    FileHelper.persistStream(new ByteArrayInputStream((objectMapper.writeValueAsString(resourceCollectionInstances)+"\n"). // knocking off resource name from the files
                            getBytes()),
                            jobMonitoringStatsPath, true);
                    resourcesLastInstance.put(resource, resourceCollectionInstance);
                    resourceLastInstance = resourceCollectionInstance;
                }
            }
            if(resourceLastInstance != null)
                FileHelper.persistStream(new ByteArrayInputStream(objectMapper.writeValueAsString(resourceLastInstance).getBytes()), jobMonitoringStatsPath+".last", false);
            resourcesLastInstance.put(resource, resourceLastInstance);
        }
        jobLastResourceMetricInstanceMap.put(jobId, resourcesLastInstance);
    }

    /**
     * Returns Monitoring Stats Meta Data. Useful to see what all monitoring stats are being collected as part of performance testing
     * @param jobId
     * @return
     */
    @Path("/{jobId}/monitoringStats")
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public List<JobHelper.MonitoringAgentStats> getJobMonitoringStatsMeta(@PathParam("jobId") String jobId) throws IOException, ExecutionException {
        jobHelper.jobExistsOrException(jobId);
        return jobHelper.getJobMonitoringStatsMeta(jobId);
    }

    /**
     * Returns stats for particular resource being monitored at particular box
     * @return
     */
    @Path("/{jobId}/monitoringStats/agents/{agent}/resources/{resourceName}")
    @GET
    @Timed
    @Produces(MediaType.TEXT_PLAIN)
    public InputStream getJobMonitoringResourceStats(@PathParam("jobId") String jobId,
                                                     @PathParam("agent") String agent,
                                                     @PathParam("resourceName") String resourceName,
                                                    @QueryParam("last") @DefaultValue("false") BooleanParam last) throws IOException, ExecutionException {
        jobHelper.jobExistsOrException(jobId);
        return jobHelper.getJobMonitoringResourceStats(jobId, agent, resourceName, last.get());
    }

    /**
     * Return resource metric Keys that are being collected
     * @param jobId
     * @return
     */
    @Path("/{jobId}/monitoringStats/agents/{agent}/resources/{resourceName}/meta")
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getJobMonitoringResourceMeta(@PathParam("jobId") String jobId,
                                                    @PathParam("agent") String agent,
                                                    @PathParam("resourceName") String resourceName) throws IOException, ExecutionException {
        jobHelper.jobExistsOrException(jobId);
        return jobHelper.getJobMonitoringResourceMeta(jobId, agent, resourceName);
    }

    @Path("/{jobId}/logs")
    @GET
    @Timed
    @Produces(MediaType.TEXT_HTML)
    public String getJobLogs(@PathParam("jobId") String jobId) throws IOException, ExecutionException {
        Job job = jobHelper.jobExistsOrException(jobId);

        StringBuilder stringBuilder = new StringBuilder();
        for(String agentIp : job.getAgentsJobStatus().keySet()) {
            stringBuilder.append("<a href=\"" + agentConfig.getJobLogUrl(jobId, agentIp) +"\">" + agentIp + ".log</a><br>");
        }
        return stringBuilder.toString();
    }

    /**
     * This resource is called by Loader agent once job is completed
     * @param request
     * @param jobId
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Path("/{jobId}/over")
    @PUT
    @Timed
    public void jobOver(@Context HttpServletRequest request,
                        @PathParam("jobId") String jobId) throws InterruptedException, ExecutionException, IOException {
        Job job = jobHelper.jobExistsOrException(jobId);
        String agentIp = request.getRemoteAddr();
        AgentsCache.getAgentInfo(agentIp).setFree();
        AgentsCache.getAgentInfo(agentIp).getRunningJobs().remove(jobId);

        job.jobCompletedInAgent(agentIp);

        if(job.completed()) {
            jobHelper.stopMonitoring(job);
            CounterCompoundThread.getCounterCruncherThread().removeJob(jobId);
            CounterThroughputThread.getCounterCruncherThread().removeJob(jobId);
            TimerComputationThread.getComputationThread().removeJob(jobId);
            job.setEndTime(new Date());
        }
        jobHelper.persistJob(job);
    }


    /**
     * Will be called from Loader Server Management UI To kill the job in all agents
     * @param jobId
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Path("/{jobId}/kill")
    @PUT
    @Timed
    public void killJob(@PathParam("jobId") String jobId) throws InterruptedException, ExecutionException, IOException, JobException {
        Job job = jobHelper.jobExistsOrException(jobId);
        jobHelper.killJobInAgents(job, job.getAgentsJobStatus().keySet());
        jobHelper.stopMonitoring(job);
        job.setEndTime(new Date());
        jobHelper.persistJob(job);
    }

    /**
     * Will be called from Loader Server Management UI To kill the job in specific agents
     * @param jobId
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     * @throws JobException
     */
    @Path("/{jobId}/agents/{agentIps}/kill")
    @PUT
    @Timed
    public void killJob(@PathParam("jobId") String jobId, @PathParam("agentIps") String agentIps) throws InterruptedException, ExecutionException, IOException, JobException {
        Job job = jobHelper.jobExistsOrException(jobId);
        jobHelper.killJobInAgents(job, Arrays.asList(agentIps.split(",")));
        if(job.completed())
            jobHelper.stopMonitoring(job);
    }

    private void runExistsOrException(String runName) {
        if(!new File(jobFSConfig.getRunPath(runName)).exists()) {
            throw new WebApplicationException(ResponseBuilder.runNameDoesNotExist(runName));
        }
    }
}