package perf.server;

/**
 * Date : 28/12/2012
 * USer : nitinka
 */

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.FilterBuilder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import perf.server.cache.AgentsCache;
import perf.server.cache.LibCache;
import perf.server.config.LoaderServerConfiguration;
import perf.server.daemon.CounterCompoundThread;
import perf.server.daemon.CounterThroughputThread;
import perf.server.daemon.JobDispatcherThread;
import perf.server.daemon.TimerComputationThread;
import perf.server.resource.*;
import perf.server.util.DeploymentHelper;
import perf.server.util.JobStatsHelper;

public class LoaderServerService extends Service<LoaderServerConfiguration> {

    @Override
    public void initialize(Bootstrap<LoaderServerConfiguration> bootstrap) {
        bootstrap.setName("loader-server");
    }

    @Override
    public void run(LoaderServerConfiguration configuration, Environment environment) throws Exception {
        environment.addProvider(com.sun.jersey.multipart.impl.MultiPartReaderServerSide.class);

        // Initialization
        LibCache.initialize(configuration.getLibStorageFSConfig());
        CounterCompoundThread.initialize(configuration.getJobFSConfig(), 10000).start();
        CounterThroughputThread.initialize(configuration.getJobFSConfig(), 10000).start();
        TimerComputationThread.initialize(configuration.getJobFSConfig(), 10000).start();
        DeploymentHelper.initialize(configuration.getAgentConfig(),
                configuration.getLibStorageFSConfig());
        AgentsCache.initialize(configuration.getAgentConfig());

        FilterBuilder filterConfig = environment.addFilter(CrossOriginFilter.class, "/*");
        filterConfig.setInitParam(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM, String.valueOf(60*60*24)); // 1 day - jetty-servlet CrossOriginFilter will convert to Int.

        JobStatsHelper.build(configuration.getJobFSConfig(), configuration.getAgentConfig(), configuration.getMonitoringAgentConfig());
        JobDispatcherThread.initialize();

        //filterConfig.setInitParam(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "http://localhost"); // comma separated list of allowed origin domains
        // Adding Resource End Points
        environment.addResource(new DeployLibResource(configuration.getLibStorageFSConfig()));
        environment.addResource(new AgentResource(configuration.getAgentConfig()));
        environment.addResource(new JobResource(configuration.getAgentConfig(),
                configuration.getMonitoringAgentConfig(),
                configuration.getJobFSConfig()));
        environment.addResource(new RunResource(configuration.getJobFSConfig()));
        environment.addResource(new FunctionResource(configuration.getLibStorageFSConfig()));
        environment.addResource(new AdminResource(configuration));
    }


    public static void main(String[] args) throws Exception {
        args = new String[]{"server",args[0]};
        new LoaderServerService().run(args);
    }

}
