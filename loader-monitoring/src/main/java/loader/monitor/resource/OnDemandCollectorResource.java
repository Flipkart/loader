package loader.monitor.resource;

import loader.monitor.collector.BaseCollector;
import loader.monitor.collector.CollectorFactory;
import loader.monitor.collector.CollectorThread;
import loader.monitor.config.OnDemandCollectorConfig;
import loader.monitor.domain.OnDemandCollector;
import loader.monitor.domain.OnDemandCollectorRequest;
import org.apache.log4j.Logger;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/onDemandResources")
public class OnDemandCollectorResource {
    private static Logger log = Logger.getLogger(OnDemandCollectorResource.class);
    private List<OnDemandCollectorConfig> onDemandCollectorConfigs;
    private Map<String, List<BaseCollector>> onDemandCollectorsRequestMap;
    private CollectorThread collectorThread;

    public OnDemandCollectorResource(List<OnDemandCollectorConfig> onDemandCollectorConfigs, CollectorThread collectorThread) throws ClassNotFoundException, NoSuchMethodException {
        this.onDemandCollectorConfigs = onDemandCollectorConfigs;
        this.onDemandCollectorsRequestMap = new HashMap<String, List<BaseCollector>>();
        this.collectorThread = collectorThread;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<OnDemandCollectorConfig> getResources() {
        return this.onDemandCollectorConfigs;
    }

    @Path("/{resource}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public OnDemandCollectorConfig getResource(@PathParam("resource") String resource) {
        for(OnDemandCollectorConfig c : this.onDemandCollectorConfigs)
            if(c.getName().equals(resource))
                return c;
        throw new WebApplicationException(404);
    }

    /**
     * {
           "requestId":"jobId1",
            "collectors":[
                {
                    "name":"df",
                    "klass":"loader.monitor.collector.DFCollector",
                    "interval":30000
                },
                {
                    "name":"df2",
                    "klass":"loader.monitor.collector.DFCollector",
                    "interval":40000
                },
                {
                    "name":"jmx1",
                    "klass":"loader.monitor.collector.jmx.JMXCollector",
                    "interval":30000,
                    "params" : {
                         "host" : "localhost",
                         "port" : 4444
                     }
                }
            ]
        }
     * @param request
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Path("/requests")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void startCollectors(@Valid OnDemandCollectorRequest request) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        startcollection(request);
    }

    private void startcollection(OnDemandCollectorRequest request) throws InvocationTargetException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        List<BaseCollector> collectors = new ArrayList<BaseCollector>();
        for(OnDemandCollector collectorInfo : request.getCollectors()) {
            BaseCollector collector = CollectorFactory.buildCollector(collectorInfo);
            if(collector.supported()) {
                this.collectorThread.startCollector(collector);
                collectors.add(collector);
            }
        }

        this.onDemandCollectorsRequestMap.put(request.getRequestId(), collectors);
    }

    @DELETE
    @Path("/requests/{requestId}")
    public void stopCollectors(@PathParam("requestId") String requestId) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if(this.onDemandCollectorsRequestMap.containsKey(requestId)) {
            for(BaseCollector collector : this.onDemandCollectorsRequestMap.get(requestId)) {
                this.collectorThread.stopCollector(collector);
            }
        }
        else
            throw new WebApplicationException(404);
    }
}

