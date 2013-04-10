package perf.agent;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.agent.cache.LibCache;
import perf.agent.client.LoaderServerClient;
import perf.agent.config.LoaderAgentConfiguration;
import perf.agent.daemon.AgentRegistrationThread;
import perf.agent.daemon.JobProcessorThread;
import perf.agent.daemon.JobStatsSyncThread;
import perf.agent.health.JobProcessorHealthCheck;
import perf.agent.resource.DeployLibResource;
import perf.agent.resource.JobResource;


public class LoaderAgentService extends Service<LoaderAgentConfiguration> {
    private static Logger logger = LoggerFactory.getLogger(LoaderAgentService.class);

    @Override
    public void initialize(Bootstrap<LoaderAgentConfiguration> bootstrap) {
        bootstrap.setName("loader-agent");
    }

    @Override
    public void run(final LoaderAgentConfiguration configuration, Environment environment) throws Exception {
        environment.addProvider(com.sun.jersey.multipart.impl.MultiPartReaderServerSide.class);
        LibCache.initialize(configuration.getLibStorageConfig());

        JobProcessorThread.initialize(configuration.getJobProcessorConfig(),
                LoaderServerClient.buildClient(configuration.getServerInfo()),
                configuration.getJobFSConfig());

        JobStatsSyncThread.initialize(configuration.getJobStatSyncConfig(),
                configuration.getJobFSConfig(),
                LoaderServerClient.buildClient(configuration.getServerInfo()));

        AgentRegistrationThread.initialize(LoaderServerClient.buildClient(configuration.getServerInfo()),
                configuration.getRegistrationParams());

        environment.addResource(new DeployLibResource(configuration.getLibStorageConfig()));
        environment.addResource(new JobResource(configuration.getJobProcessorConfig(),
                configuration.getJobFSConfig()));
        environment.addHealthCheck(new JobProcessorHealthCheck("JobProcessorThread"));
    }


    public static void main(String[] args) throws Exception {
        args = new String[]{"server",args[0]};
        new LoaderAgentService().run(args);
    }

}
