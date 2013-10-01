package perf.server.dataFix;

import com.open.perf.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.server.config.JobFSConfig;
import perf.server.config.LoaderServerConfiguration;
import perf.server.domain.PerformanceRun;
import perf.server.util.ObjectMapperUtil;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 26/6/13
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveWarmUpTimeFromGroupsFixer implements DataFixer {

    private static Logger log = LoggerFactory.getLogger(RemoveWarmUpTimeFromGroupsFixer.class);

    @Override
    public boolean fix(LoaderServerConfiguration configuration) {
        JobFSConfig jobFSconfig = configuration.getJobFSConfig();
        File runsPath = new File(jobFSconfig.getRunsPath());

        for(File runPath : runsPath.listFiles())  {
            try {
                String runName = runPath.getName();
                log.info("Fixing run :"+runName);

                String runDetailsString = FileHelper.readContent(new FileInputStream(jobFSconfig.getRunFile(runName)));
                runDetailsString = runDetailsString.replace("\"warmUpTime\" : -1,", "").replace("\"warmUpTime\":-1,", "");
                PerformanceRun run = ObjectMapperUtil.instance().readValue(runDetailsString, PerformanceRun.class);
                run.persist();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return true;
    }
}
