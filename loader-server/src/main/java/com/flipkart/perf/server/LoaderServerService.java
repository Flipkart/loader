package com.flipkart.perf.server;

/**
 * Date : 28/12/2012
 * USer : nitinka
 */

import com.flipkart.perf.server.auth.User;
import com.flipkart.perf.server.auth.dummy.SimpleAuthenticator;
import com.flipkart.perf.server.daemon.*;
import com.flipkart.perf.server.domain.WorkflowScheduler;
import com.flipkart.perf.server.health.*;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.auth.basic.BasicAuthProvider;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.FilterBuilder;
import nitinka.jmetrics.JMetric;
import nitinka.jmetrics.controller.dropwizard.JMetricController;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import com.flipkart.perf.server.cache.AgentsCache;
import com.flipkart.perf.server.cache.LibCache;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.dataFix.DataFixRunner;
import com.flipkart.perf.server.resource.*;
import com.flipkart.perf.server.util.DeploymentHelper;
import com.flipkart.perf.server.util.JobStatsHelper;
import com.flipkart.perf.server.cache.JobsCache;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class LoaderServerService extends Service<LoaderServerConfiguration> {

    private ScheduledExecutorService scheduledExecutorService;

    public LoaderServerService() {
    }
	
    @Override
    public void initialize(Bootstrap<LoaderServerConfiguration> bootstrap) {
        bootstrap.setName("loader-server");
				bootstrap.addBundle(new AssetsBundle("/assets","/","/index.html"));
    }

    @Override
    public void run(LoaderServerConfiguration configuration, Environment environment) throws Exception {
        // Read more here https://github.com/klauern/ldap-dropwizard-roles
        // Read more here http://gary-rowe.com/agilestack/2012/10/23/multibit-merchant-implementing-hmac-authentication-in-dropwizard/
//        environment.addProvider(new BasicAuthProvider<User>(new SimpleAuthenticator(),
//                "SUPER SECRET STUFF"));

        // Generic Stuff
        FilterBuilder filterConfig = environment.addFilter(CrossOriginFilter.class, "/*");
        filterConfig.setInitParam(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM, String.valueOf(60*60*24)); // 1 day - jetty-servlet CrossOriginFilter will convert to Int.

        environment.addProvider(com.sun.jersey.multipart.impl.MultiPartReaderServerSide.class);

        // Do Data Fixes
        new DataFixRunner(configuration.getDataFixConfig()).run();

        // Cache Initialization
        JobsCache.initiateCache(configuration.getJobFSConfig());
        LibCache.initialize(configuration.getResourceStorageFSConfig());
        AgentsCache.initialize(configuration.getAgentConfig());

        JobStatsHelper.build(configuration.getJobFSConfig(), configuration.getAgentConfig(), configuration.getMonitoringAgentConfig());

        // Start the Scheduled Executor
        this.scheduledExecutorService = Executors.newScheduledThreadPool(configuration.getScheduledExecutorConfig().getThreadPoolSize());
        // initialize Daemon Services

        HistogramComputationThread.initialize(configuration.getJobFSConfig(), 10000).start();
        CounterCompoundThread.initialize(scheduledExecutorService, configuration.getJobFSConfig(), configuration.getScheduledExecutorConfig().getCounterCompoundThreadInterval());
//        CounterThroughputThread.initialize(scheduledExecutorService, configuration.getJobFSConfig(), configuration.getScheduledExecutorConfig().getCounterThroughputThreadInterval());
        GroupConfConsolidationThread.initialize(scheduledExecutorService, configuration.getJobFSConfig(), configuration.getScheduledExecutorConfig().getGroupConfConsolidationThreadInterval());
        JobDispatcherThread.initialize(scheduledExecutorService, configuration.getScheduledExecutorConfig().getJobDispatcherThreadInterval());
        TimerComputationThread.initialize(scheduledExecutorService, configuration.getJobFSConfig(), configuration.getScheduledExecutorConfig().getTimerComputationThreadInterval());

        DeploymentHelper.initialize(configuration.getAgentConfig(),
                configuration.getResourceStorageFSConfig());


        ScheduledWorkflowDispatcherThread.initialize();
        Thread workFlowDispatcher = new Thread(ScheduledWorkflowDispatcherThread.getInstance());
        workFlowDispatcher.start();
        WorkflowScheduler.initialize();

//        JMetric.initialize(configuration.getjMetricConfig());
//        environment.addResource(new JMetricController());

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
        environment.addResource(new SampleAuthenticatedResource());
        environment.addHealthCheck(new CounterCompoundThreadHealthCheck("CounterCompoundThread"));
        environment.addHealthCheck(new TimerComputationThreadHealthCheck("TimerComputationThread"));
    }

    public static void main(String[] args) throws Exception {
        args = new String[]{"server",args[0]};
        new LoaderServerService().run(args);
    }

}
