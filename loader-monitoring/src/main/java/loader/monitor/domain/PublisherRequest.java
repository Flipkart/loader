package loader.monitor.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import loader.monitor.cache.ResourceCache;
import loader.monitor.collector.ResourceCollectionInstance;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 24/1/13
 * Time: 4:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublisherRequest {
    private String requestId;
    private Set<String> resources;
    private int lastHowManyInstances;
    private String publishUrl; // post url
    private long forHowLong; // ms
    private long interval = 60000; // ms
    private long startTime = -1;

    public String  getRequestId() {
        return requestId;
    }

    public PublisherRequest setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public Set<String> getResources() {
        return resources;
    }

    public PublisherRequest setResources(Set<String> resources) {
        this.resources = resources;
        return this;
    }

    public int getLastHowManyInstances() {
        return lastHowManyInstances;
    }

    public PublisherRequest setLastHowManyInstances(int lastHowManyInstances) {
        this.lastHowManyInstances = lastHowManyInstances;
        return this;
    }

    public String getPublishUrl() {
        return publishUrl;
    }

    public PublisherRequest setPublishUrl(String publishUrl) {
        this.publishUrl = publishUrl;
        return this;
    }

    public long getForHowLong() {
        return forHowLong;
    }

    public PublisherRequest setForHowLong(long forHowLong) {
        this.forHowLong = forHowLong;
        return this;
    }

    @JsonIgnore
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    @JsonIgnore
    public void publish() throws IOException, ExecutionException, InterruptedException {
        if(this.startTime == -1) {
            this.startTime = System.currentTimeMillis();
        }

        Map<String, List<ResourceCollectionInstance>> resourceCollectionInstances = new HashMap<String, List<ResourceCollectionInstance>>();
        for(String resoucre : this.resources) {
            resourceCollectionInstances.put(resoucre, ResourceCache.getStats(resoucre, this.lastHowManyInstances));
        }

        String metrics = new ObjectMapper().writeValueAsString(resourceCollectionInstances);
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.preparePost(this.publishUrl).
                addHeader("Content-Type", "application/json").
                setBody(metrics);

        Future<Response> r = b.execute();
        r.get();
        asyncHttpClient.close();
    }

    @JsonIgnore
    public boolean requestExpired() {
        if(forHowLong > -1) {
            if(System.currentTimeMillis() - this.startTime > forHowLong)
                return false;
        }
        return true;
    }
}
