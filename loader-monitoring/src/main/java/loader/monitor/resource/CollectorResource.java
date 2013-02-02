package loader.monitor.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.metrics.annotation.Timed;
import loader.monitor.cache.ResourceCache;
import loader.monitor.collector.ResourceCollectionInstance;
import loader.monitor.domain.Metric;
import loader.monitor.util.FileHelper;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.resource.ResourceCollection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/resources")
public class CollectorResource {
    private static Logger log = Logger.getLogger(CollectorResource.class);
    private static List<String> ignoreSubResourceList = Arrays.asList(new String[]{"cpu.cpu"});

    /**
     * Get All Resources
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    synchronized public Set getResources() throws IOException, InterruptedException {
        return ResourceCache.getResources();
    }

    // resource can be a comma separated value
    @Path("/{resources}")
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    synchronized public Map getResource(@PathParam("resources") String resourcesStr,
                                        @QueryParam("count") @DefaultValue("1") IntParam count) throws IOException, InterruptedException {
        String[] resources = resourcesStr.split(",");
        Map<String, List<ResourceCollectionInstance>> resourceCollectionInstances = new HashMap<String, List<ResourceCollectionInstance>>();
        for(String resoucre : resources) {
            resourceCollectionInstances.put(resoucre, ResourceCache.getStats(resoucre, count.get()));
        }
        return resourceCollectionInstances;
    }

    @POST
    @Timed
    synchronized public void addMetric(@PathParam("resource") String resource, String metricFromCollector) throws IOException, InterruptedException {
        Map<String, ResourceCollectionInstance> resourcMetrics = metricHash(metricFromCollector);
        log.info(new ObjectMapper().writeValueAsString(resourcMetrics));
        cacheResources(resourcMetrics);
    }

    private void cacheResources(Map<String, ResourceCollectionInstance> resourcMetrics) {
        for(String resource : resourcMetrics.keySet()) {
            ResourceCache.addStats(resourcMetrics.get(resource));
        }
    }

    private static Map<String,ResourceCollectionInstance> metricHash(String metricFromCollector) {
        String[] metricLines = metricFromCollector.split("\n");
        log.info("Metric Lines :"+metricLines.length);
        Map<String,ResourceCollectionInstance> resourceMetrics = new HashMap<String, ResourceCollectionInstance>();


        for(String metricLine : metricLines) {
            if(!metricLine.trim().equals("")) {
                long time = Long.parseLong(metricLine.split(" ")[2]);

                log.info("Line :"+metricLine);
                String[] lineTokens = metricLine.split(" ");
                String[] keyTokens = lineTokens[0].split("\\.");

                String resource = keyTokens[2];

                if(keyTokens.length == 5) {
                    resource += "."+keyTokens[3];
                }

                if(ignoreResource(resource)) {
                    //log.debug("Ignoring "+resource);
                    continue;
                }

                resource += "@"+time;
                String metricName = keyTokens[keyTokens.length-1];
                Double metricValue = Double.parseDouble(lineTokens[1]);
                Metric metric = new Metric().setName(metricName).setValue(metricValue);

                ResourceCollectionInstance collectionInstance = resourceMetrics.get(resource);

                collectionInstance = collectionInstance == null ?
                        new ResourceCollectionInstance().
                                setResourceName(resource.split("@")[0]).
                                setTime(time) :
                        collectionInstance;
                collectionInstance.addMetric(metric);

                resourceMetrics.put(resource, collectionInstance);
           }
        }
        return resourceMetrics;
    }

    private static boolean ignoreResource(String resourceMetric) {
        for(String ignoreMetric : ignoreSubResourceList)
            if(resourceMetric.startsWith(ignoreMetric))
                return true;
        return false;
    }

    public static void main(String[] args) throws IOException {
        Map<String, ResourceCollectionInstance> stats = metricHash(FileHelper.fileContent("./sample/metricData.txt"));
    }

}

