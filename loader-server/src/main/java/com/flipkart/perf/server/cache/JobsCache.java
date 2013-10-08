package com.flipkart.perf.server.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.flipkart.perf.server.config.JobFSConfig;
import com.flipkart.perf.server.domain.Job;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 26/6/13
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobsCache {
    private static ObjectMapper objectMapper;
    private static LoadingCache<String, Job> jobs;
    private static Logger logger = LoggerFactory.getLogger(Job.class);

    static {
        objectMapper = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("MMM dd hh:mm:ss z yyyy");
        objectMapper.setDateFormat(dateFormat);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    public static void initiateCache(final JobFSConfig jobFSConfig) {
        jobs = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build(
                        new CacheLoader<String, Job>() {
                            public Job load(String jobId) throws IOException {
                                File jobStatusFile = new File(jobFSConfig.getJobStatusFile(jobId));
                                if(jobStatusFile.exists())
                                    return objectMapper.readValue(jobStatusFile, Job.class);
                                logger.error("Status File Not Found for Job Id '"+jobId+"'");
                                return null;
                            }
                        });
    }

    public static Job getJob(String jobId) throws ExecutionException {
        try {
            return jobs.get(jobId);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            if(e.getLocalizedMessage().contains("null"))
                return null;
        }
        return null;
    }

    public static void put(String jobId, Job job) {
        jobs.put(jobId, job);
    }
}
