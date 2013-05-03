package perf.agent.resource;

import com.open.perf.util.FileHelper;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.metrics.annotation.Timed;
import perf.agent.cache.LibCache;
import perf.agent.config.JobFSConfig;
import perf.agent.config.JobProcessorConfig;
import perf.agent.daemon.JobProcessorThread;
import perf.agent.job.Job;
import perf.agent.util.ResponseBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Receive Requests about jobs
 */
@Path("/jobs")

public class JobResource {
    private JobProcessorThread jobProcessorThread = JobProcessorThread.instance();
    private JobProcessorConfig jobProcessorConfig;
    private final JobFSConfig jobFSconfig;

    public JobResource(JobProcessorConfig jobProcessorConfig, JobFSConfig jobFSConfig) {
        this.jobProcessorConfig = jobProcessorConfig;
        this.jobFSconfig = jobFSConfig;
    }

    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String queueJob(@FormDataParam("jobId") String jobId,
                           @FormDataParam("jobJson") InputStream jobJson,
                           @FormDataParam("classList") String classListStr) throws IOException {

        List<String> classList = Arrays.asList(classListStr.split("\n"));

        String jobClassPath = LibCache.getInstance().
                buildJobClassPath(classList);

        String jobCMD = this.jobProcessorConfig.getJobCLIFormat().
                replace("{classpath}", jobClassPath).
                replace("{jobJson}", ""+ FileHelper.persistStream(jobJson, "/tmp/" + System.currentTimeMillis())).
                replace("{jobId}", jobId);

        Job job = new Job().
                setJobCmd(jobCMD).
                setJobId(jobId);

        jobProcessorThread.addJobRequest(job);
        return job.getJobId();
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
    public void kill(@PathParam("jobId") String jobId) throws IOException, InterruptedException {
        jobProcessorThread.killJob(jobId);
    }

    @Produces(MediaType.TEXT_HTML)
    @Path("/{jobId}/log")
    @GET
    @Timed
    public InputStream log(@PathParam("jobId") String jobId,
                      @QueryParam("lines") @DefaultValue("100") IntParam lines,
                      @QueryParam("grep") @DefaultValue("") String grepExp)
            throws IOException, InterruptedException {
        if(jobExists(jobId)) {
            String jobLogFile = jobFSconfig.getJobLogFile(jobId);
            if(new File(jobLogFile).exists()) {

                // Build Command
                StringBuilder cmdBuilder = new StringBuilder();
                if(lines.get().intValue() > 0) {
                    cmdBuilder.append("tail -"+lines.get().intValue() + " " + jobLogFile);
                }

                if(!grepExp.trim().equals("")) {
                    if(cmdBuilder.toString().equals("")) {
                        cmdBuilder.append(" grep "+grepExp + " " + jobLogFile);
                    }
                    else {
                        cmdBuilder.append(" | grep "+grepExp);
                    }
                }

                // Execute Command
                if(!cmdBuilder.equals("")) {
                    cmdBuilder.append(" | sed 's/$/<br>/'");
                    String[] cmd = {
                            "/bin/sh",
                            "-c",
                            cmdBuilder.toString()
                    };

                    Process process = Runtime.getRuntime().exec(cmd);
                    process.waitFor();
                    return process.getInputStream();
                }
            }
        }
        else
            throw new WebApplicationException(ResponseBuilder.jobNotFound(jobId));
        return new ByteArrayInputStream("".getBytes());
    }

    private boolean jobExists(String jobId) {
        return new File(jobFSconfig.getJobPath(jobId)).exists();
    }
}