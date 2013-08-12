package perf.server;

/**
 * Date : 28/12/2012
 * USer : nitinka
 */

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.FilterBuilder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import perf.server.cache.AgentsCache;
import perf.server.cache.LibCache;
import perf.server.config.LoaderServerConfiguration;
import perf.server.daemon.*;
import perf.server.dataFix.DataFixRunner;
import perf.server.domain.BusinessUnit;
import perf.server.health.CounterCompoundThreadHealthCheck;
import perf.server.health.TimerComputationThreadHealthCheck;
import perf.server.resource.*;
import perf.server.util.DeploymentHelper;
import perf.server.util.JobStatsHelper;
import perf.server.cache.JobsCache;

public class LoaderServerService extends Service<LoaderServerConfiguration> {

	public LoaderServerService() {
	}
	
    @Override
    public void initialize(Bootstrap<LoaderServerConfiguration> bootstrap) {
        bootstrap.setName("loader-server");
				bootstrap.addBundle(new AssetsBundle("/assets","/","/index.html"));
    }

    @Override
    public void run(LoaderServerConfiguration configuration, Environment environment) throws Exception {
        environment.addProvider(com.sun.jersey.multipart.impl.MultiPartReaderServerSide.class);

        // Do Data Fixes
        new DataFixRunner(configuration.getDataFixConfig()).run();

        // Initialization
        JobsCache.initiateCache(configuration.getJobFSConfig());
        LibCache.initialize(configuration.getResourceStorageFSConfig());
        CounterCompoundThread.initialize(configuration.getJobFSConfig(), 10000).start();
        CounterThroughputThread.initialize(configuration.getJobFSConfig(), 10000).start();
        TimerComputationThread.initialize(configuration.getJobFSConfig(), 10000).start();
        GroupConfConsolidationThread.initialize(configuration.getJobFSConfig(), 10000).start();
        DeploymentHelper.initialize(configuration.getAgentConfig(),
                configuration.getResourceStorageFSConfig());
        AgentsCache.initialize(configuration.getAgentConfig());

        FilterBuilder filterConfig = environment.addFilter(CrossOriginFilter.class, "/*");
        filterConfig.setInitParam(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM, String.valueOf(60*60*24)); // 1 day - jetty-servlet CrossOriginFilter will convert to Int.

        JobStatsHelper.build(configuration.getJobFSConfig(), configuration.getAgentConfig(), configuration.getMonitoringAgentConfig());
        JobDispatcherThread.initialize();

        environment.addResource(new JobResource(configuration.getAgentConfig(),
                configuration.getJobFSConfig()));
        environment.addResource(new DeployResourcesResource(configuration.getResourceStorageFSConfig()));
        environment.addResource(new AgentResource(configuration.getAgentConfig()));
        environment.addResource(new RunResource(configuration.getJobFSConfig()));
        environment.addResource(new FunctionResource(configuration.getResourceStorageFSConfig()));
        environment.addResource(new BusinessUnitResource(configuration.getJobFSConfig()));
        environment.addResource(new AdminResource(configuration));
        environment.addHealthCheck(new CounterCompoundThreadHealthCheck("CounterCompoundThread"));
        environment.addHealthCheck(new TimerComputationThreadHealthCheck("TimerComputationThread"));
    }


    public static void main(String[] args) throws Exception {
        args = new String[]{"server",args[0]};
        new LoaderServerService().run(args);
    }

}
