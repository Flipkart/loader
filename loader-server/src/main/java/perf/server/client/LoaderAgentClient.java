package perf.server.client;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.multipart.FilePart;
import com.ning.http.multipart.StringPart;
import org.apache.log4j.Logger;
import perf.server.cache.LibCache;
import perf.server.exception.JobException;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    static {
        libCache = LibCache.getInstance();
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

    public LoaderAgentClient deployPlatformLibs() throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePost("http://" + this.getHost() + ":" + this.getPort() + RESOURCE_PLATFORM_LIB).
                setHeader("Content-Type", MediaType.MULTIPART_FORM_DATA).
                addBodyPart(new FilePart("lib", new File(libCache.getPlatformZipPath())));

        Future<Response> r = b.execute();
        if(!r.isDone())
            r.get();

        asyncHttpClient.close();
        return this;
    }

    public LoaderAgentClient deployOperationLibs(String classList) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        Map<String,String> libClassListMap = makeLibClassListMap(classList);

        for(String lib : libClassListMap.keySet()) {
            AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                    preparePost("http://" + this.getHost() + ":" + this.getPort() + RESOURCE_OPERATION_LIB).
                    setHeader("Content-Type", MediaType.MULTIPART_FORM_DATA).
                    addBodyPart(new FilePart("lib", new File(lib))).
                    addBodyPart(new StringPart("classList", libClassListMap.get(lib)));

            Future<Response> r = b.execute();
            r.get();
        }
        asyncHttpClient.close();
        return this;
    }

    public void submitJob(String jobId, String jobJson, String classListStr) throws ExecutionException, InterruptedException, JobException {
        //logger.debug("Job Id :"+jobId);
        //logger.debug("Job Json :"+jobJson);
        //logger.debug("Class List Str :"+classListStr);

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePost("http://"+this.getHost()+":" +
                        this.getPort() +
                        RESOURCE_JOB).
                setHeader("Content-Type", MediaType.MULTIPART_FORM_DATA).
                addBodyPart(new StringPart("jobId", jobId)).
                addBodyPart(new StringPart("jobJson", jobJson)).
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

    private Map<String, String> makeLibClassListMap(String classList) {

        Map<String,String> libClassListMap = new HashMap<String, String>();
        List<String> libsRequired = new ArrayList<String>();
        for(String className : classList.split("\n")) {
            libsRequired.add(libCache.getLibsMapWithClassAsKey().get(className));
        }

        for(String libRequired : libsRequired) {
            String libClassListStr = "";
            List<String> libClassList = libCache.getLibsMapWithLibAsKey().get(libRequired);
            for(String libClass : libClassList)
                libClassListStr += libClass + "\n";
            libClassListMap.put(libRequired, libClassListStr.trim());
        }
        return libClassListMap;
    }



}
