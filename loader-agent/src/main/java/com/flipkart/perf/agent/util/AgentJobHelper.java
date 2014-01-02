package com.flipkart.perf.agent.util;

import com.flipkart.perf.agent.config.LoaderAgentConfiguration;
import com.flipkart.perf.agent.job.AgentJob;
import com.flipkart.perf.common.jackson.ObjectMapperUtil;

import javax.ws.rs.WebApplicationException;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 7/10/13
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgentJobHelper {
    private static LoaderAgentConfiguration configuration = LoaderAgentConfiguration.instance();
    public static AgentJob jobExistsOrException(String jobId) throws IOException {
        File jobFile = new File(configuration.getJobFSConfig().getJobFile(jobId));
        if(!jobFile.exists())
            throw new WebApplicationException(ResponseBuilder.jobNotFound(jobId));
        return ObjectMapperUtil.instance().readValue(jobFile, AgentJob.class);
    }
}
