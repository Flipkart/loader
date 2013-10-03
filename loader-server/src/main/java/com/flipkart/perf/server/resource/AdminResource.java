package com.flipkart.perf.server.resource;


import java.io.File;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.config.ReportConfig;
import com.flipkart.perf.server.util.ObjectMapperUtil;

import com.yammer.metrics.annotation.Timed;

/**
 * Create Various runs. Think of them as Performance Flows
 */
@Path("/admin")
public class AdminResource {

    private final LoaderServerConfiguration configuration;

    public AdminResource(LoaderServerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/config/report/job")
    @Timed
    public ReportConfig getReportConfig() throws IOException {
        return ObjectMapperUtil.instance().readValue(new File(configuration.getReportConfigFile()), ReportConfig.class);
    }

    @POST
    @Path("/config/report/job")
    @Timed
    public void saveReportConfig(ReportConfig reportConfig) throws IOException {
        ObjectMapperUtil.instance().writerWithDefaultPrettyPrinter().writeValue(new File(configuration.getReportConfigFile()), reportConfig);
    }

}