package perf.server.resource;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.multipart.FilePart;
import com.ning.http.multipart.StringPart;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.metrics.annotation.Timed;
import org.apache.log4j.Logger;
import perf.server.cache.LibCache;
import perf.server.config.AgentConfig;
import perf.server.util.FileHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Path("/agents")

public class AgentResource {
    private static Logger log = Logger.getLogger(AgentResource.class);
    private LibCache libCache;
    private AgentConfig agentConfig;

    public AgentResource(AgentConfig agentConfig) {
        this.libCache = LibCache.getInstance();
        this.agentConfig = agentConfig;
    }

    @Path("/{agentIPs}/libs/platformLibs")
    @POST
    @Timed
    synchronized public void deployPlatformLib(
            @PathParam("agentIPs") String agentIPs) throws IOException, ExecutionException, InterruptedException {
        for(String agentIP : agentIPs.split(","))
            deployPlatformLibs(agentIP);
    }

/*
    Following call simulates html form post call
    curl
       -X POST
       -H "Content-Type: multipart/form-data"
       -F "classList=@Path-To-File-Containing-Class-Names-Separated-With-New-Line"
       http://localhost:9999/loader-server/agents/{comma separated ips}/libs/classLibs
*/
    
    @Path("/{agentIPs}/libs/classLibs")
    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    synchronized public void deployClassLib(
            @FormDataParam("classList") InputStream classListInputStream,
            @PathParam("agentIPs") String agentIPs) throws IOException, ExecutionException, InterruptedException {
        String classes = FileHelper.readContent(classListInputStream);
        for(String agentIP : agentIPs.split(","))
            deployibs(agentIP, classes);
    }


    private void deployPlatformLibs(String agentIP) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePost("http://"+agentIP+":" + agentConfig.getAgentPort() + agentConfig.getPlatformLibResource()).
                setHeader("Content-Type", "multipart/form-data").
                addBodyPart(new FilePart("lib",new File(libCache.getPlatformLibPath())));

        Future<Response> r = b.execute();
        if(!r.isDone())
            r.get();

        asyncHttpClient.close();
    }

    private void deployibs(String agentIP, String classList) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        Map<String,String> libClassListMap = makeLibClassListMap(classList);

        for(String lib : libClassListMap.keySet()) {
            AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                    preparePost("http://" + agentIP + ":" + agentConfig.getAgentPort() + agentConfig.getClassLibResource()).
                    setHeader("Content-Type", "multipart/form-data").
                    addBodyPart(new FilePart("lib", new File(lib))).
                    addBodyPart(new StringPart("classList", libClassListMap.get(lib)));

            Future<Response> r = b.execute();
            r.get();
        }
        asyncHttpClient.close();
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