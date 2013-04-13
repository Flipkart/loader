package perf.server.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.open.perf.jackson.ObjectMapperUtil;
import com.open.perf.util.FileHelper;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.metrics.annotation.Timed;
import org.codehaus.jackson.map.ObjectMapper;
import perf.server.config.JobFSConfig;
import perf.server.exception.JobException;
import perf.server.util.ResponseBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
     curl
     -X POST
     -H "Content-Type: multipart/form-data"
     -F "runJson=@Path-To-File-Containing-Job-Json"
     http://localhost:9999/loader-server/runs
     * @param runJsonInfoStream
     * @throws IOException
     * @throws java.util.concurrent.ExecutionException
     * @throws InterruptedException
     */
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    @Timed
    public void createRun(@FormDataParam("runJson") InputStream runJsonInfoStream)
            throws IOException, ExecutionException, InterruptedException, JobException {
        persistRunInfo(runJsonInfoStream);
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
        runExistsOrException(runName);
        return FileHelper.readContent(new FileInputStream(jobFSConfig.getRunFile(runName)));
    }

    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl
     -X PUT
     -H "Content-Type: multipart/form-data"
     -F "runJson=@Path-To-File-Containing-Job-Json"
     http://localhost:9999/loader-server/runs/runName
     * @param runJsonInfoStream
     * @throws IOException
     * @throws java.util.concurrent.ExecutionException
     * @throws InterruptedException
     */
    @Path("/{runName}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @PUT
    @Timed
    public void updateRun(@PathParam("runName") String runName,
                          @FormDataParam("runJson") InputStream runJsonInfoStream)
            throws IOException, ExecutionException, InterruptedException, JobException {
        updateRunInfo(runName, runJsonInfoStream);
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

    private void persistRunInfo(InputStream jobJsonInfoStream) throws IOException {
        JsonNode jobInfoJsonNode = objectMapper.readValue(jobJsonInfoStream, JsonNode.class);
        String runName = jobInfoJsonNode.get("runName").textValue();
        if(new File(jobFSConfig.getRunPath(runName)).exists()) {
            throw new WebApplicationException(ResponseBuilder.runNameAlreadyExists(runName));
        }

        String runFile = jobFSConfig.getRunFile(runName);
        FileHelper.createFilePath(runFile);
        FileHelper.persistStream(new ByteArrayInputStream(jobInfoJsonNode.toString().getBytes()),
                runFile,
                false);
    }

    private void updateRunInfo(String runName, InputStream jobJsonInfoStream) throws IOException {
        JsonNode jobInfoJsonNode = objectMapper.readValue(jobJsonInfoStream, JsonNode.class);
        runExistsOrException(runName);
        FileHelper.persistStream(new ByteArrayInputStream(jobInfoJsonNode.toString().getBytes()),
                jobFSConfig.getRunFile(runName),
                false);
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