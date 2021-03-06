package com.flipkart.server.monitor.resource;

import com.flipkart.server.monitor.cache.ResourceCache;
import com.flipkart.server.monitor.domain.ResourceCollectionInstance;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;

/**
 * Enable user to see what all resources are being collected at the agent monitoring service side.
 */
@Path("/resources")
public class CollectorResource {
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
        for(String resource : resources) {
            resourceCollectionInstances.put(resource, ResourceCache.getStats(resource, count.get()));
        }
        return resourceCollectionInstances;
    }

    /**
     * Mostly used to delete resources that were created as on demand resources
     * Resources can be comma separated resources
     */
    @Path("/{resources}")
    @DELETE
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    synchronized public void deleteResource(@PathParam("resources") String resourcesStr) throws IOException, InterruptedException {
        String[] resources = resourcesStr.split(",");
        for(String resource : resources) {
            ResourceCache.removeResource(resource);
        }
    }

    @POST
    @Timed
    synchronized public void addMetric(String metricFromCollector) throws IOException, InterruptedException {
        Map<String, ResourceCollectionInstance> resourceMetrics = metricHash(metricFromCollector);
        cacheResources(resourceMetrics);
    }

    private void cacheResources(Map<String, ResourceCollectionInstance> resourceMetrics) {
        for(String resource : resourceMetrics.keySet()) {
            ResourceCache.addStats(resourceMetrics.get(resource));
        }
    }

    private static Map<String,ResourceCollectionInstance> metricHash(String metricFromCollector) {
        String[] metricLines = metricFromCollector.split("\n");
        //log.debug("Metric Lines :"+metricLines.length);
        Map<String,ResourceCollectionInstance> resourceMetrics = new HashMap<String, ResourceCollectionInstance>();


        for(String metricLine : metricLines) {
            if(!metricLine.trim().equals("")) {
                long time = Long.parseLong(metricLine.split(" ")[2]) * 1000; // As Diamond Return time in second.

                //log.info("debug :"+metricLine);
                String[] lineTokens = metricLine.split(" ");
                String[] keyTokens = lineTokens[0].split("\\.");

                String resource = keyTokens[2];

                if(keyTokens.length == 5) {
                    resource += "."+keyTokens[3];
                }

                if(ignoreResource(resource)) {
                    continue;
                }

                resource += "@"+time;
                String metricName = keyTokens[keyTokens.length-1];
                Double metricValue = Double.parseDouble(lineTokens[1]);

                ResourceCollectionInstance collectionInstance = resourceMetrics.get(resource);

                collectionInstance = collectionInstance == null ?
                        new ResourceCollectionInstance().
                                setResourceName(resource.split("@")[0]).
                                setTime(time) :
                        collectionInstance;
                collectionInstance.addMetric(metricName, metricValue);

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

}

