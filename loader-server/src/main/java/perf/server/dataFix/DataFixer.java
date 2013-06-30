package perf.server.dataFix;

import perf.server.LoaderServerService;
import perf.server.config.JobFSConfig;
import perf.server.config.LibStorageFSConfig;
import perf.server.config.LoaderServerConfiguration;

import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 26/6/13
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DataFixer {
    public boolean fix(LoaderServerConfiguration configuration);
}
