package perf.server.resource;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;

import perf.server.config.JobFSConfig;
import perf.server.domain.PerformanceRun;
import perf.server.exception.JobException;
import perf.server.util.ObjectMapperUtil;
import perf.server.util.ResponseBuilder;

import com.open.perf.util.FileHelper;
import com.yammer.metrics.annotation.Timed;

/**
 * Create Various runs. Think of them as Performance Flows
 */
@Path("/runs")
public class RunResource {

    private final JobFSConfig jobFSConfig;
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();

    public RunResource(JobFSConfig jobFSConfig) {
        this.jobFSConfig = jobFSConfig;
    }

    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl -X POST -d @file-containing-run-details http://localhost:9999/loader-server/runs --header "Content-Type:application/json"
     *
     * @param performanceRun
     * @throws IOException
     * @throws java.util.concurrent.ExecutionException
     * @throws InterruptedException
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Timed
    public Response createRun(PerformanceRun performanceRun)
            throws IOException, ExecutionException, InterruptedException, JobException {
        persistRunInfo(performanceRun);
        return ResponseBuilder.resourceCreated("Run", performanceRun.getRunName());
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
    public PerformanceRun getRun(@PathParam("runName") String runName) throws IOException {
        runExistsOrException(runName);
        return objectMapper.readValue(new File(jobFSConfig.getRunFile(runName)), PerformanceRun.class);
    }

    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl -X PUT -d @file-containing-run-details http://localhost:9999/loader-server/runs/runName --header "Content-Type:application/json"
     * @param performanceRun
     * @throws IOException
     * @throws java.util.concurrent.ExecutionException
     * @throws InterruptedException
     */
    @Path("/{runName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    @Timed
    public void updateRun(@PathParam("runName") String runName,
                          PerformanceRun performanceRun)
            throws IOException, ExecutionException, InterruptedException, JobException {
        updateRunInfo(runName, performanceRun);
    }

    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl
     -X DELETE
     http://localhost:9999/loader-server/runs/runName
     * @throws IOException
     * @throws java.util.concurrent.ExecutionException
     * @throws InterruptedException
     */
    @Path("/{runName}")
    @DELETE
    @Timed
    public void deleteRun(@PathParam("runName") String runName)
            throws IOException, ExecutionException, InterruptedException, JobException {
        deleteRunInfo(runName);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/{runName}/jobs")
    @Timed
    public Set<String> getRunJobs(@PathParam("runName") String runName) throws IOException {
        runExistsOrException(runName);
        Set<String> jobs = new HashSet<String>();
        String runJobsFile = jobFSConfig.getRunJobsFile(runName);
        BufferedReader br = FileHelper.bufferedReader(runJobsFile);
        String jobId = null;
        while((jobId = br.readLine()) != null)
            jobs.add(jobId);
        FileHelper.close(br);
        return jobs;
    }

    private void persistRunInfo(PerformanceRun performanceRun) throws IOException {
        String runName = performanceRun.getRunName();
        if(new File(jobFSConfig.getRunPath(runName)).exists()) {
            throw new WebApplicationException(ResponseBuilder.runNameAlreadyExists(runName));
        }

        String runFile = jobFSConfig.getRunFile(runName);
        FileHelper.createFilePath(runFile);
        objectMapper.writeValue(new File(runFile), performanceRun);
    }

    private void updateRunInfo(String runName, PerformanceRun performanceRun) throws IOException {
        runExistsOrException(runName);
        objectMapper.writeValue(new File(jobFSConfig.getRunFile(runName)), performanceRun);
    }

    private void deleteRunInfo(String runName) {
        runExistsOrException(runName);
        FileHelper.remove(jobFSConfig.getRunPath(runName));
    }

    private void runExistsOrException(String runName) {
        if(!new File(jobFSConfig.getRunPath(runName)).exists()) {
            throw new WebApplicationException(ResponseBuilder.runNameDoesNotExist(runName));
        }
    }

}