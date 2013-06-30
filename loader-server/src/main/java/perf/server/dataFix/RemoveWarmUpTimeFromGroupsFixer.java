package perf.server.dataFix;

import com.open.perf.util.FileHelper;
import perf.server.config.JobFSConfig;
import perf.server.config.LoaderServerConfiguration;
import perf.server.domain.PerformanceRun;
import perf.server.util.ObjectMapperUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 26/6/13
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveWarmUpTimeFromGroupsFixer implements DataFixer {

    @Override
    public boolean fix(LoaderServerConfiguration configuration) {
        JobFSConfig jobFSconfig = configuration.getJobFSConfig();
        File runsPath = new File(jobFSconfig.getRunsPath());

        for(File runPath : runsPath.listFiles())  {
            try {
                String runDetailsString = FileHelper.readContent(new FileInputStream(jobFSconfig.getRunFile(runPath.getName())));
                runDetailsString = runDetailsString.replace("\"warmUpTime\" : -1,", "");
                PerformanceRun run = ObjectMapperUtil.instance().readValue(runDetailsString, PerformanceRun.class);
                run.persist();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return false;
            }
        }
        return true;
    }
}
