package perf.server.dataFix;

import com.open.perf.util.FileHelper;
import org.apache.log4j.Logger;
import perf.server.config.JobFSConfig;
import perf.server.config.LoaderServerConfiguration;
import perf.server.domain.LoadPart;
import perf.server.domain.PerformanceRun;
import perf.server.util.ObjectMapperUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class AddInputFileResourceToLoadPartFixer implements DataFixer {

    private static Logger log = Logger.getLogger(AddInputFileResourceToLoadPartFixer.class);

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
                log.info("Fixing run :"+run.getRunName());
                run.persist();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return true;
    }
}
