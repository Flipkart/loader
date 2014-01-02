package com.flipkart.perf.server.dataFix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.flipkart.perf.server.config.JobFSConfig;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.domain.LoadPart;
import com.flipkart.perf.server.domain.PerformanceRun;
import com.flipkart.perf.server.util.ObjectMapperUtil;

import java.io.File;
import java.util.ArrayList;

public class AddInputFileResourceToLoadPartFixer implements DataFixer {

    private static Logger logger = LoggerFactory.getLogger(AddInputFileResourceToLoadPartFixer.class);

    @Override
    public boolean fix(LoaderServerConfiguration configuration) {
        JobFSConfig jobFSconfig = configuration.getJobFSConfig();
        File runsPath = new File(jobFSconfig.getRunsPath());

        for(File runPath : runsPath.listFiles())  {
            try {
                PerformanceRun run = ObjectMapperUtil.instance().
                        readValue(new File(jobFSconfig.getRunFile(runPath.getName())), PerformanceRun.class);
                for(LoadPart loadPart : run.getLoadParts()) {
                    if(loadPart.getInputFileResources() == null)
                        loadPart.setInputFileResources(new ArrayList<String>());
                }
                logger.info("Fixing run :"+run.getRunName());
                run.persist();
            } catch (Exception e) {
                logger.error("Error while running data fix", e);
            }
        }
        return true;
    }
}
