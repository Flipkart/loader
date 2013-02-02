package perf.server.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.multipart.StringPart;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.metrics.annotation.Timed;
import perf.server.config.AgentConfig;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Path("/jobs")
public class JobResource {

    private AgentConfig agentConfig;

    public JobResource(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }
    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl
     -X POST
     -H "Content-Type: multipart/form-data"
     -F "jobJson=@Path-To-File-Containing-Job-Json"
     -F "classList=@Path-To-File-Containing-Classes-Separated-With-New-Line"
     http://localhost:8888/loader-server/jobs
     * @param jobJsonInfoStream
     * @param classListStr
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Timed
    public Map createJob(@FormDataParam("jobJson") InputStream jobJsonInfoStream,
                               @FormDataParam("classList") String classListStr)
            throws IOException, ExecutionException, InterruptedException {
        String jobId = UUID.randomUUID().toString();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jobInfo = mapper.readValue(jobJsonInfoStream,JsonNode.class);
        ArrayNode jobParts = (ArrayNode) jobInfo.get("jobParts");

        System.out.println(jobInfo);
        System.out.println(jobParts);

        for(int i=0; i<jobParts.size(); i++) {

            ObjectNode jobPart = (ObjectNode) jobParts.get(i);
            submitJob(jobId,
                    jobPart.get("agent").textValue(),
                    jobPart.get("jobPartInfo").toString(),
                    classListStr);
        }
        Map<String,Object> job = new HashMap<String, Object>();
        job.put("jobId", jobId);
        return job;
    }

    private void submitJob(String jobId, String agent, String jobPartInfo, String classListStr)
            throws IOException, ExecutionException, InterruptedException {

        System.out.println("Job Json "+jobPartInfo);
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePost("http://"+agent+":" +
                        agentConfig.getAgentPort() +
                        agentConfig.getJobResource()).
                setHeader("Content-Type", "multipart/form-data").
                addBodyPart(new StringPart("jobId", jobId)).
                addBodyPart(new StringPart("jobJson", jobPartInfo)).
                addBodyPart(new StringPart("classList", classListStr));

        Future<Response> r = b.execute();
        if(!r.isDone())
            r.get();

        asyncHttpClient.close();
    }

}