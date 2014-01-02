package com.flipkart.perf.server.resource;

import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.server.auth.User;
import com.flipkart.perf.server.cache.JobsCache;
import com.flipkart.perf.server.config.AgentConfig;
import com.flipkart.perf.server.config.JobFSConfig;
import com.flipkart.perf.server.daemon.JobDispatcherThread;
import com.flipkart.perf.server.domain.Job;
import com.flipkart.perf.server.domain.JobRequest;
import com.flipkart.perf.server.domain.PerformanceRun;
import com.flipkart.perf.server.domain.ResourceCollectionInstance;
import com.flipkart.perf.server.exception.JobException;
import com.flipkart.perf.server.util.JobStatsHelper;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import com.flipkart.perf.server.util.ResponseBuilder;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.jersey.params.BooleanParam;
import com.yammer.metrics.annotation.Timed;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Resource that receive Performance Job Request from Client Lib or Loader-Server UI
 */
@Path("/checkAuth")
public class SampleAuthenticatedResource {

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    public String getJobs(@Auth User user) {
        return "User " + user.getName() + " is authenticated";
    }
}
