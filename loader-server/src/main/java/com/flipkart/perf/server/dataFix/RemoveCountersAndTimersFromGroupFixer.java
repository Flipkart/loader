package com.flipkart.perf.server.dataFix;

import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.server.config.JobFSConfig;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.domain.PerformanceRun;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 26/6/13
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveCountersAndTimersFromGroupFixer implements DataFixer {

    private static Logger log = LoggerFactory.getLogger(RemoveCountersAndTimersFromGroupFixer.class);

    @Override
    public boolean fix(LoaderServerConfiguration configuration) {
        JobFSConfig jobFSconfig = configuration.getJobFSConfig();
        File runsPath = new File(jobFSconfig.getRunsPath());

        for(File runPath : runsPath.listFiles())  {
            try {
                String runName = runPath.getName();
                log.info("Fixing run :"+runName);
                String runDetailsString = FileHelper.readContent(new FileInputStream(jobFSconfig.getRunFile(runName)));
                runDetailsString = runDetailsString.replace("\"customTimers\" : [],", "").replace("\"\"customCounters\": []", "");
                PerformanceRun run = ObjectMapperUtil.instance().readValue(runDetailsString, PerformanceRun.class);
                run.persist();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return true;
    }
}
