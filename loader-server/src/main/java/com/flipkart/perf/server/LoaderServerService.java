package com.flipkart.perf.server;

/**
 * Date : 28/12/2012
 * USer : nitinka
 */

import com.flipkart.perf.server.daemon.*;
import com.flipkart.perf.server.domain.WorkflowScheduler;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.FilterBuilder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import com.flipkart.perf.server.cache.AgentsCache;
import com.flipkart.perf.server.cache.LibCache;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.dataFix.DataFixRunner;
import com.flipkart.perf.server.health.CounterCompoundThreadHealthCheck;
import com.flipkart.perf.server.health.TimerComputationThreadHealthCheck;
import com.flipkart.perf.server.resource.*;
import com.flipkart.perf.server.util.DeploymentHelper;
import com.flipkart.perf.server.util.JobStatsHelper;
import com.flipkart.perf.server.cache.JobsCache;

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
//        CounterThroughputThread.initialize(configuration.getJobFSConfig(), 10000).start();
        TimerComputationThread.initialize(configuration.getJobFSConfig(), 10000).start();
        HistogramComputationThread.initialize(configuration.getJobFSConfig(), 10000).start();
        GroupConfConsolidationThread.initialize(configuration.getJobFSConfig(), 10000).start();
        DeploymentHelper.initialize(configuration.getAgentConfig(),
                configuration.getResourceStorageFSConfig());
        AgentsCache.initialize(configuration.getAgentConfig());

        FilterBuilder filterConfig = environment.addFilter(CrossOriginFilter.class, "/*");
        filterConfig.setInitParam(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM, String.valueOf(60*60*24)); // 1 day - jetty-servlet CrossOriginFilter will convert to Int.

        JobStatsHelper.build(configuration.getJobFSConfig(), configuration.getAgentConfig(), configuration.getMonitoringAgentConfig());

        JobDispatcherThread.initialize();

        ScheduledWorkflowDispatcherThread.initialize();
        Thread workFlowDispatcher = new Thread(ScheduledWorkflowDispatcherThread.getInstance());
        workFlowDispatcher.start();
        WorkflowScheduler.initialize();

        environment.addResource(new JobResource(configuration.getAgentConfig(),
                configuration.getJobFSConfig()));
        environment.addResource(new DeployResourcesResource(configuration.getResourceStorageFSConfig()));
        environment.addResource(new AgentResource(configuration.getAgentConfig()));
        environment.addResource(new RunResource(configuration.getJobFSConfig()));
        environment.addResource(new FunctionResource(configuration.getResourceStorageFSConfig()));
        environment.addResource(new BusinessUnitResource(configuration.getJobFSConfig()));
        environment.addResource(new AdminResource(configuration));
        environment.addResource(new ScheduledWorkflowResource());
        environment.addResource(new WorkflowJobResource());
        environment.addHealthCheck(new CounterCompoundThreadHealthCheck("CounterCompoundThread"));
        environment.addHealthCheck(new TimerComputationThreadHealthCheck("TimerComputationThread"));
    }

    public static void main(String[] args) throws Exception {
        args = new String[]{"server",args[0]};
        new LoaderServerService().run(args);
    }

}
