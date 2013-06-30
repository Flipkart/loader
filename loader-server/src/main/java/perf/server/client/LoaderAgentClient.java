package perf.server.client;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import perf.server.cache.LibCache;
import perf.server.exception.JobException;
import perf.server.exception.LibNotDeployedException;
import perf.server.util.ObjectMapperUtil;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.multipart.FilePart;
import com.ning.http.multipart.StringPart;
import com.open.perf.domain.Load;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 7/2/13
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class LoaderAgentClient {
    private String host;
    private int port;
    private static LibCache libCache;
    private static Logger logger = Logger.getLogger(LoaderAgentClient.class);

    private static final String RESOURCE_PLATFORM_LIB = "/loader-agent/libs/platformLibs";
    private static final String RESOURCE_OPERATION_LIB = "/loader-agent/libs/classLibs";
    private static final String RESOURCE_JOB = "/loader-agent/jobs";
    private static final String RESOURCE_JOB_KILL = "/loader-agent/jobs/{jobId}/kill";
    private static final String RESOURCE_ADMIN_REGISTRATION_INFO = "/loader-agent/admin/registrationInfo";
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();

    static {
        libCache = LibCache.instance();
    }

    public LoaderAgentClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public LoaderAgentClient setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public LoaderAgentClient setPort(int port) {
        this.port = port;
        return this;
    }

    public Map registrationInfo() throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                prepareGet("http://" + this.getHost() + ":" + this.getPort() + RESOURCE_ADMIN_REGISTRATION_INFO);

        Future<Response> r = b.execute();
        Response response = r.get();
        asyncHttpClient.close();
        return objectMapper.readValue(response.getResponseBodyAsStream(), Map.class);
    }

    public boolean deployPlatformLibs() throws IOException, ExecutionException, InterruptedException, LibNotDeployedException {
        if(libCache.getPlatformZipPath() == null) {
            throw new LibNotDeployedException("Platform Lib Not Deployed Yet on Loader Server. Deploy them before submitting another job");
        }
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePost("http://" + this.getHost() + ":" + this.getPort() + RESOURCE_PLATFORM_LIB).
                setHeader("Content-Type", MediaType.MULTIPART_FORM_DATA).
                addBodyPart(new FilePart("lib", new File(libCache.getPlatformZipPath())));

        Future<Response> r = b.execute();
        if(!r.isDone())
            r.get();

        boolean successfulDeployment = r.get().getStatusCode() == 200;
        asyncHttpClient.close();
        return successfulDeployment;
    }

    public boolean deployClassLibs(String libPath, String classList) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePost("http://" + this.getHost() + ":" + this.getPort() + RESOURCE_OPERATION_LIB).
                setHeader("Content-Type", MediaType.MULTIPART_FORM_DATA).
                addBodyPart(new FilePart("lib", new File(libPath))).
                addBodyPart(new StringPart("classList", classList));

        Future<Response> r = b.execute();
        r.get();
        boolean successfulDeployment = r.get().getStatusCode() == 204;
        asyncHttpClient.close();
        return successfulDeployment;
    }

    public void submitJob(String jobId, Load load, String classListStr)
            throws ExecutionException, InterruptedException, JobException, IOException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePost("http://"+this.getHost()+":" +
                        this.getPort() +
                        RESOURCE_JOB).
                setHeader("Content-Type", MediaType.MULTIPART_FORM_DATA).
                addBodyPart(new StringPart("jobId", jobId)).
                addBodyPart(new StringPart("jobJson", objectMapper.writeValueAsString(load))).
                addBodyPart(new StringPart("classList", classListStr));

        try {
            Future<Response> r = b.execute();
            r.get();
            if(r.get().getStatusCode() != 200) {
                throw new JobException("JobId "+jobId+" submission failed with error response :"+r.get().getResponseBody());
            }
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            asyncHttpClient.close();
        }
    }

    public void killJob(String jobId) throws ExecutionException, InterruptedException, JobException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePut("http://"+this.getHost()+":" +
                        this.getPort() +
                        RESOURCE_JOB_KILL.
                                replace("{jobId}", jobId));
        try {
            Future<Response> r = b.execute();
            r.get();
            if(r.get().getStatusCode() != 200) {
                throw new JobException("JobId "+jobId+" kill failed with error response :"+r.get().getResponseBody());
            }
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            asyncHttpClient.close();
        }
    }
}
