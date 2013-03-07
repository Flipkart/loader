package perf.agent.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.open.perf.util.FileHelper;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.metrics.annotation.Timed;
import perf.agent.cache.LibCache;
import perf.agent.config.JobProcessorConfig;
import perf.agent.job.JobInfo;
import perf.agent.daemon.JobProcessorThread;
import perf.agent.daemon.StatSyncThread;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Receive Requests to run job on agent
 */
@Path("/jobs")

public class JobResource {
    private JobProcessorThread jobProcessorThread = JobProcessorThread.getInstance();
    private JobProcessorConfig jobProcessorConfig;
    private StatSyncThread statsSyncThread;
    private static ObjectMapper mapper = new ObjectMapper();

    public JobResource(JobProcessorConfig jobProcessorConfig) {
        this.jobProcessorConfig = jobProcessorConfig;
        this.statsSyncThread = StatSyncThread.getInstance();
    }

    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String startJob(@FormDataParam("jobId") String jobId,
            @FormDataParam("jobJson") InputStream jobJson,
            @FormDataParam("classList") String classListStr) throws IOException {

        List<String> classList = mapper.readValue(classListStr, List.class);

        String jobClassPath = LibCache.getInstance().
                buildJobClassPath(classList);
        String jobCMD = this.jobProcessorConfig.getJobCLIFormat().
                replace("$CLASSPATH", jobClassPath).
                replace("$JOB_JSON", ""+ FileHelper.persistStream(jobJson, "/tmp/" + System.currentTimeMillis())).
                replace("$JOB_ID", jobId);

        JobInfo jobInfo = new JobInfo().
                setJobCmd(jobCMD).
                setJobId(jobId);

        jobProcessorThread.addJobRequest(jobInfo);
        return jobInfo.getJobId();
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Map getJobs(@QueryParam("status") @DefaultValue("") String jobStatus) {
        return jobProcessorThread.
                getJobs(jobStatus);
    }

    @Path("/{jobId}/kill")
    @PUT
    @Timed
    public String pause(@PathParam("jobId") String jobId) {
        String killStatus = jobProcessorThread.killJob(jobId);
        statsSyncThread.removeJob(jobId);
        return "{'message' : '"+killStatus+"'}";
    }
}