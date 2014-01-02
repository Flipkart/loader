package com.flipkart.perf.server.dataFix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.flipkart.perf.server.config.JobFSConfig;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.domain.BusinessUnit;
import com.flipkart.perf.server.domain.PerformanceRun;
import com.flipkart.perf.server.domain.Team;
import com.flipkart.perf.server.util.ObjectMapperUtil;

import java.io.File;

public class AssignDefaultBusinessUnitForExistingRuns implements DataFixer {

    private static Logger logger = LoggerFactory.getLogger(AssignDefaultBusinessUnitForExistingRuns.class);

    @Override
    public boolean fix(LoaderServerConfiguration configuration) {
        JobFSConfig jobFSconfig = configuration.getJobFSConfig();
        File runsPath = new File(jobFSconfig.getRunsPath());

        for(File runPath : runsPath.listFiles())  {
            try {
                PerformanceRun run = ObjectMapperUtil.instance().
                        readValue(new File(jobFSconfig.getRunFile(runPath.getName())), PerformanceRun.class);
                logger.info("Fixing run :"+run.getRunName());

                BusinessUnit businessUnit = BusinessUnit.build(run.getBusinessUnit());
                Team team = businessUnit.getTeam(run.getTeam());
                if(!team.getRuns().contains(run.getRunName())) {
                    team.getRuns().add(run.getRunName());
                    businessUnit.persist();
                }
            } catch (Exception e) {
                logger.error("Error while running data fix", e);
            }
        }
        return true;
    }
}
