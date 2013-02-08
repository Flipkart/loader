package perf.agent.client;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 7/2/13
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoaderServerClient {
    private String host;
    private int port;
    private static final String RESOURCE_JOB_OVER = "/loader-server/jobs/{jobId}/over";
    private static final String RESOURCE_JOB_STATS = "/loader-server/jobs/{jobId}/jobStats?file={file}";

    private static Logger log = Logger.getLogger(LoaderServerClient.class);

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

    public void notifyJobIsOver(String jobId) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePut("http://" + this.getHost() + ":" +
                        this.getPort() +
                        RESOURCE_JOB_OVER.
                                replace("{jobId}", jobId)).
                setHeader("Content-Type", MediaType.APPLICATION_JSON);

        Future<Response> r = b.execute();
        r.get();
        if(r.get().getStatusCode() != 204) {
            log.error("Delete on "+RESOURCE_JOB_OVER.
                    replace("{jobId}", jobId));
        }
        asyncHttpClient.close();

    }

    public void publishJobStats(String jobId, String filePath, String linesRead) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePost("http://" +
                        this.getHost() +
                        ":" +
                        this.getPort() +
                        RESOURCE_JOB_STATS.
                                replace("{jobId}", jobId).
                                replace("{file}", filePath)).
                setBody(linesRead);

        Future<Response> r = b.execute();
        r.get();
        asyncHttpClient.close();
    }
}
