package loader.monitor.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.metrics.annotation.Timed;
import loader.monitor.cache.ResourceCache;
import loader.monitor.collector.ResourceCollectionInstance;
import loader.monitor.domain.Metric;
import loader.monitor.domain.PublisherRequest;
import loader.monitor.publisher.PublisherThread;
import loader.monitor.util.FileHelper;
import org.apache.log4j.Logger;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/publishResourcesRequests")
public class PublishRequestResource {
    private static Map<String, PublisherRequest> publisherRequestMap;
    private static Logger log;
    private PublisherThread publisherThread;

    public PublishRequestResource(PublisherThread publisherThread) {
        this.publisherThread = publisherThread;
    }

    static {
        publisherRequestMap = new HashMap<String, PublisherRequest>();
        log = Logger.getLogger(PublishRequestResource.class);
    }

    /**
      {
         "requestId" : "jobId1",
         "resources" : ["cpu.total","jmx1"],
         "lastHowManyInstances" : 1,
         "publishUrl" : "http://localhost:9999/monitoring-service/dummy",
         "interval" : 60000

      }
     */

    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Timed
    synchronized public void addRequest(@Valid PublisherRequest publisherRequest) throws IOException, InterruptedException {
        publisherThread.addRequest(publisherRequest);
        publisherRequestMap.put(publisherRequest.getRequestId(), publisherRequest);
    }

    @Path("requestId")
    @DELETE
    @Timed
    synchronized public void removeRequest(@PathParam("requestId") String requestId) throws IOException, InterruptedException {
        PublisherRequest publisherRequest = publisherRequestMap.remove(requestId);
        if(publisherRequest != null)
            publisherThread.removeRequest(publisherRequest);
        else
            throw new WebApplicationException(404);
    }
}

