package com.flipkart.perf.agent.client;

import com.flipkart.perf.agent.util.AgentJobHelper;
import com.flipkart.perf.common.jackson.ObjectMapperUtil;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Client to interact with Loader Server
 */
public class JobClient {
    private static final String RESOURCE_JOB_KILL = "/loader-core/kill";

    private static Logger logger = LoggerFactory.getLogger(JobClient.class);
    private static AsyncHttpClient httpClient = new AsyncHttpClient();

    /**
     * Kill Job
     * @param jobId
     * @throws java.io.IOException
     * @throws java.util.concurrent.ExecutionException
     * @throws InterruptedException
     */
    public static void killJob(String jobId) throws ExecutionException, InterruptedException, IOException {
        int jobHttpPort = AgentJobHelper.jobExistsOrException(jobId).getHttpPort();
        logger.info("Killing Job "+ jobId +" running with http port "+jobHttpPort);
        AsyncHttpClient.BoundRequestBuilder b = httpClient.
                preparePut("http://localhost:" +
                        jobHttpPort +
                        RESOURCE_JOB_KILL);

        Future<Response> r = null;
        try {
            r = b.execute();
        } catch (IOException e) {
            logger.error("Error While Contacting Job to Kill. eating away the exception assuming job can't be contacted and is dead", e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        r.get();
        if(r.get().getStatusCode() != 204) {
            logger.error("Delete on "+RESOURCE_JOB_KILL +" gave "+r.get().getStatusCode() + " http status code");
        }
    }
}
