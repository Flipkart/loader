package server.monitor.resource;

import com.open.perf.jackson.ObjectMapperUtil;
import com.yammer.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.monitor.domain.MetricPublisherRequest;
import server.monitor.publisher.MetricPublisherThread;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/publishResourcesRequests")
public class PublishRequestResource {
    private static Map<String, MetricPublisherRequest> publisherRequestMap;
    private static Logger logger = LoggerFactory.getLogger(PublishRequestResource.class);
    private MetricPublisherThread metricPublisherThread;

    public PublishRequestResource(MetricPublisherThread metricPublisherThread) {
        this.metricPublisherThread = metricPublisherThread;
    }

    static {
        publisherRequestMap = new HashMap<String, MetricPublisherRequest>();
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
    synchronized public void addRequest(@Valid MetricPublisherRequest metricPublisherRequest) throws IOException, InterruptedException {
        logger.info("Publisher Request :" + ObjectMapperUtil.instance().writeValueAsString(metricPublisherRequest));
        metricPublisherThread.addRequest(metricPublisherRequest);
        publisherRequestMap.put(metricPublisherRequest.getRequestId(), metricPublisherRequest);
    }

    @Path("/{requestId}")
    @DELETE
    @Timed
    synchronized public void removeRequest(@PathParam("requestId") String requestId) throws IOException, InterruptedException {
        logger.info("All Publisher Requests :"+publisherRequestMap.toString());
        MetricPublisherRequest metricPublisherRequest = publisherRequestMap.remove(requestId);
        if(metricPublisherRequest != null)
            metricPublisherThread.removeRequest(metricPublisherRequest);
        else
            throw new WebApplicationException(404);
    }
}

