package loader.monitor.cache;

import loader.monitor.collector.ResourceCollectionInstance;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 4/1/13
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceCache {
    private static final int MAX_CACHE_SIZE = 10000;
    private static Map<String,List<ResourceCollectionInstance>> resourceStatsCache;

    static {
        resourceStatsCache = new HashMap<String, List<ResourceCollectionInstance>>();
    }

    synchronized public static void addStats(ResourceCollectionInstance currentCollectionInstance) {
        List<ResourceCollectionInstance> cachedCollectionInstances = resourceStatsCache.get(currentCollectionInstance.getResourceName());
        if(cachedCollectionInstances == null) {
            cachedCollectionInstances = new ArrayList<ResourceCollectionInstance>();
        }
        else {
            if(cachedCollectionInstances.size() == MAX_CACHE_SIZE) {
                cachedCollectionInstances.remove(0);
            }
        }
        ResourceCollectionInstance prevCollectionInstance = null;

        // Taking care of the situation where some of the metrics for the resource with old time came now.
        if(cachedCollectionInstances.size() > 0) {
            prevCollectionInstance = cachedCollectionInstances.get(cachedCollectionInstances.size()-1);
            if(prevCollectionInstance.getTime().equals(currentCollectionInstance.getTime())) {
                prevCollectionInstance.addMetrics(currentCollectionInstance.getMetrics());
                return;
            }
        }

        cachedCollectionInstances.add(currentCollectionInstance);
        resourceStatsCache.put(currentCollectionInstance.getResourceName(), cachedCollectionInstances);
    }

    synchronized public static List<ResourceCollectionInstance> getStats(String resource, int howMany) {
        List<ResourceCollectionInstance> toSendBack = new ArrayList<ResourceCollectionInstance>();
        List<ResourceCollectionInstance> resourceStats = resourceStatsCache.get(resource);
        if(resourceStats != null && resourceStats.size() > 1) {
            for(int last=resourceStats.size()-2; last >= 0 && howMany > 0; last--, howMany--) {
                toSendBack.add(resourceStats.get(last));
            }
        }
        return toSendBack;
    }

    synchronized public static Set<String> getResources() {
        return resourceStatsCache.keySet();
    }

}
