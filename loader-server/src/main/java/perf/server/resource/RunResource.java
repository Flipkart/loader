package perf.server.resource;

import com.open.perf.util.FileHelper;
import com.yammer.metrics.annotation.Timed;
import perf.server.config.JobFSConfig;
import perf.server.util.ResponseBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Create Various runs. Think of them as Performance Flows
 */
@Path("/runs")
public class RunResource {

    private final JobFSConfig jobFSConfig;

    public RunResource(JobFSConfig jobFSConfig) {
        this.jobFSConfig = jobFSConfig;
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    public Set<String> getRuns() throws IOException {
        Set<String> runs = new HashSet<String>();
        File runsPath = new File(jobFSConfig.getRunsPath());
        File[] runFolders = runsPath.listFiles();
        for(File runFolder: runFolders)
            runs.add(runFolder.getName());
        return runs;
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path(value = "/{runName}")
    @Timed
    public String getRun(@PathParam("runName") String runName) throws IOException {
        if(new File(jobFSConfig.getRunFile(runName)).exists()) {
            return FileHelper.readContent(new FileInputStream(jobFSConfig.getRunFile(runName)));
        }
        throw new WebApplicationException(ResponseBuilder.response(Response.Status.NOT_FOUND, "Run With name "+runName+" not found"));
    }


    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/{runName}/jobs")
    @Timed
    public Set<String> getRunJobs() throws IOException {
        Set<String> jobs = new HashSet<String>();
        File jobsPath = new File(jobFSConfig.getJobsPath());
        File[] jobFolders = jobsPath.listFiles();
        for(File jobFolder: jobFolders)
            jobs.add(jobFolder.getName());
        return jobs;
    }

}