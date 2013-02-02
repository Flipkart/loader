package perf.agent.resource;

import com.sun.jersey.multipart.FormDataParam;
import com.yammer.metrics.annotation.Timed;
import org.apache.log4j.Logger;
import perf.agent.cache.LibCache;
import perf.agent.config.JobProcessorConfig;
import perf.agent.job.JobInfo;
import perf.agent.job.JobProcesser;
import perf.agent.job.StatSyncThread;
import perf.agent.util.FileHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import perf.agent.util.FileHelper;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/jobs")

public class JobResource {
    private JobProcesser jobProcesser = JobProcesser.getInstance();
    private JobProcessorConfig jobProcessorConfig;
    private StatSyncThread statsSyncThread;

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

        String jobClassPath = LibCache.getInstance().
                buildJobClassPath(classListStr);
        String jobCMD = this.jobProcessorConfig.getJobCLIFormat().
                replace("$CLASSPATH", jobClassPath).
                replace("$JOB_JSON", ""+FileHelper.persistStream(jobJson,"/tmp/"+System.currentTimeMillis())).
                replace("$JOB_ID", jobId);

        JobInfo jobInfo = new JobInfo().
                setJobCmd(jobCMD).
                setJobId(jobId);

        jobProcesser.jobRequest(jobInfo);
        return jobInfo.getJobId();
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Map getJobs(@QueryParam("status") @DefaultValue("") String jobStatus) {
        return jobProcesser.
                getJobs(jobStatus);
    }

    @Path("/{jobId}/kill")
    @PUT
    @Timed
    public String pause(@PathParam("jobId") String jobId) {
        String killStatus = jobProcesser.killJob(jobId);
        statsSyncThread.removeJob(jobId);
        return "{'message' : '"+killStatus+"'}";
    }
}