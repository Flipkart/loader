package perf.server.dataFix;

import perf.server.config.JobFSConfig;
import perf.server.config.LibStorageFSConfig;
import perf.server.config.LoaderServerConfiguration;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 26/6/13
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveWarmUpTimeFromGroups implements DataFix {

    @Override
    public void fix(LoaderServerConfiguration configuration) {
        JobFSConfig jobFSconfig = configuration.getJobFSConfig();
        File runsPath = new File(jobFSconfig.getRunsPath());

        for(File runPath : runsPath.listFiles())  {

        }

    }
}
