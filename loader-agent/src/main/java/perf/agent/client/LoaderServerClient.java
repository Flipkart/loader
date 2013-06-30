package perf.agent.client;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.open.perf.jackson.ObjectMapperUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.agent.config.ServerInfo;

import javax.ws.rs.core.MediaType;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Client to interact with Loader Server
 */
public class LoaderServerClient {
    private String host;
    private int port;
    private static final String RESOURCE_JOB_OVER = "/loader-server/jobs/{jobId}/over?jobStatus={jobStatus}";
    private static final String RESOURCE_JOB_HEALTH_STATUS = "/loader-server/jobs/{jobId}/healthStatus";
    private static final String RESOURCE_JOB_STATS = "/loader-server/jobs/{jobId}/jobStats?file={file}";
    private static final String RESOURCE_AGENTS = "/loader-server/agents";

    private static Logger logger = LoggerFactory.getLogger(LoaderServerClient.class);
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();
    public LoaderServerClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public LoaderServerClient setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public LoaderServerClient setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Register Agent to the server
     * @param registrationParams
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    public void register(Map<String, Object> registrationParams)
            throws ExecutionException, InterruptedException, IOException {

        logger.info("Registering to Loader Server");
        ObjectNode node = objectMapper.createObjectNode();
        for(String key : registrationParams.keySet())
        node.put(key, registrationParams.get(key).toString());

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePost("http://" + this.getHost() + ":" +
                        this.getPort() +
                        RESOURCE_AGENTS).
                setHeader("Content-Type", MediaType.APPLICATION_JSON).
                setBody(node.toString());

        Future<Response> r = b.execute();
        r.get();
        if(r.get().getStatusCode() != 200) {
            logger.error("Post on "+RESOURCE_AGENTS);
        }
        else {
            logger.info("Registration Succeeded");
            logger.info(r.get().getResponseBody());
        }

        asyncHttpClient.close();
    }

    /**
     * Notify the Loader server that job instance on this Loader Agent is over
     * @param jobId
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void notifyJobIsOver(String jobId, String jobStatus) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePut("http://" + this.getHost() + ":" +
                        this.getPort() +
                        RESOURCE_JOB_OVER.
                                replace("{jobId}", jobId).
                                replace("{jobStatus}", jobStatus)).
                setHeader("Content-Type", MediaType.APPLICATION_JSON);

        Future<Response> r = b.execute();
        r.get();
        if(r.get().getStatusCode() != 204) {
            logger.error("Delete on "+RESOURCE_JOB_OVER.
                    replace("{jobId}", jobId));
        }
        asyncHttpClient.close();
    }

    /**
     * Publish Job Stats to Loader Server
     * @param jobId
     * @param filePath
     * @param trimmedFileName
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void publishJobStats(String jobId, String filePath, String trimmedFileName) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePost("http://" +
                        this.getHost() +
                        ":" +
                        this.getPort() +
                        RESOURCE_JOB_STATS.
                                replace("{jobId}", jobId).
                                replace("{file}", trimmedFileName)).
                setBody(new FileInputStream(filePath));

        b.execute().get();
        asyncHttpClient.close();
    }

    public static LoaderServerClient buildClient(ServerInfo serverInfo) {
        return new LoaderServerClient(serverInfo.getHost(), serverInfo.getPort());
    }

    public void notifyJobHealth(String jobId, String jobHealthStatus) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePut("http://" +
                        this.getHost() +
                        ":" +
                        this.getPort() +
                        RESOURCE_JOB_HEALTH_STATUS.
                                replace("{jobId}", jobId)).
                setBody(jobHealthStatus);
        b.execute().get();
        asyncHttpClient.close();
    }

    public void deRegister() throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                prepareDelete("http://" +
                        this.getHost() +
                        ":" +
                        this.getPort() +
                        RESOURCE_AGENTS);
        b.execute().get();
        asyncHttpClient.close();
    }
}
