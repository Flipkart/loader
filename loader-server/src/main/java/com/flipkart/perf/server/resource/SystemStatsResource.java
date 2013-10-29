package com.flipkart.perf.server.resource;

import com.flipkart.perf.common.constant.MathConstant;
import com.flipkart.perf.common.util.Clock;
import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.server.cache.AgentsCache;
import com.flipkart.perf.server.config.AgentConfig;
import com.flipkart.perf.server.domain.LoaderAgent;
import com.flipkart.perf.server.exception.LibNotDeployedException;
import com.flipkart.perf.server.health.metric.MetricArchivingEngine;
import com.flipkart.perf.server.util.AgentHelper;
import com.flipkart.perf.server.util.DeploymentHelper;
import com.flipkart.perf.server.util.ResponseBuilder;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.dropwizard.jersey.params.BooleanParam;
import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Resource to manage operations on loader-agents
 */
@Path("/systemStats")
public class SystemStatsResource {
    private static Logger logger = LoggerFactory.getLogger(SystemStatsResource.class);
    private final MetricArchivingEngine metricArchivingEngine;

    public SystemStatsResource(MetricArchivingEngine metricArchivingEngine) {
        this.metricArchivingEngine = metricArchivingEngine;
    }

    @Produces(MediaType.APPLICATION_XHTML_XML)
    @GET
    @Timed
    synchronized public Collection<String> getSystemStats()
            throws IOException, ExecutionException, InterruptedException {
        return metricArchivingEngine.metrics();
    }

    @Produces(MediaType.TEXT_HTML)
    @GET
    @Timed
    @Path("/img")
    synchronized public String getAllSystemStatImages(@Context HttpServletRequest request)
            throws IOException, ExecutionException, InterruptedException {
        List<String> metrics = metricArchivingEngine.metrics();
        Collections.sort(metrics);

        StringBuilder html = new StringBuilder("");
        Collections.sort(new ArrayList<String>());
        for(String metric : metrics) {
            html.append("<img src=\"http://"+request.getLocalAddr()+":"+request.getLocalPort()+"/loader-server/systemStats/"+metric+"/img\"/>\n");
        }
        return html.toString().trim();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    @Path("/{metricName}/raw")
    synchronized public String getMetricAsRaw(@PathParam("metricName") String metricName, @QueryParam("startTime") LongParam startTime, @QueryParam("endTime") LongParam endTime)
            throws IOException, ExecutionException, InterruptedException {
        long startTimeSec = startTime == null ? (Clock.milliTick() - 2 * 24 * 60 * 60 * 1000) / MathConstant.THOUSAND: startTime.get();
        long endTimeSec = endTime == null ? Clock.milliTick() / MathConstant.THOUSAND : endTime.get();
        return metricArchivingEngine.fetchMetrics(metricName, "TOTAL", startTimeSec, endTimeSec);
    }

    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @GET
    @Timed
    @Path("/{metricName}/img")
    synchronized public InputStream getMetricAsImage(@PathParam("metricName") String metricName, @QueryParam("startTime") LongParam startTime, @QueryParam("endTime") LongParam endTime)
            throws IOException, ExecutionException, InterruptedException {
        long startTimeSec = startTime == null ? (Clock.milliTick() - 12 * 60 * 60 * 1000) / MathConstant.THOUSAND: startTime.get();
        long endTimeSec = endTime == null ? Clock.milliTick() / MathConstant.THOUSAND : endTime.get();
        return metricArchivingEngine.fetchMetricsImage(metricName, "TOTAL", startTimeSec, endTimeSec);
    }
}