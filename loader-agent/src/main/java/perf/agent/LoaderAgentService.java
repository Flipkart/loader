package perf.agent;

/**
 * Date : 28/12/2012
 * USer : nitinka
 */
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import perf.agent.cache.LibCache;
import perf.agent.config.JobStatSyncConfig;
import perf.agent.config.LoaderAgentConfiguration;
import perf.agent.config.ServerInfo;
import perf.agent.filter.JobFilter;
import perf.agent.health.JobProcessorHealthCheck;
import perf.agent.job.JobProcesser;
import perf.agent.job.StatSyncThread;
import perf.agent.resource.DeployLibResource;
import perf.agent.resource.DummyServerResource;
import perf.agent.resource.JobResource;

public class LoaderAgentService extends Service<LoaderAgentConfiguration> {

    @Override
    public void initialize(Bootstrap<LoaderAgentConfiguration> bootstrap) {
        bootstrap.setName("loader-agent");
    }

    @Override
    public void run(LoaderAgentConfiguration configuration, Environment environment) throws Exception {
        environment.addProvider(com.sun.jersey.multipart.impl.MultiPartReaderServerSide.class);
        LibCache.initialize(configuration.getLibStorageConfig());
        JobProcesser.initialize(configuration.getJobProcessorConfig());
        StatSyncThread.initialize(configuration.getJobStatSyncConfig(), configuration.getServerInfo());

        environment.addResource(new DeployLibResource(configuration.getLibStorageConfig()));
        environment.addResource(new JobResource(configuration.getJobProcessorConfig()));
        environment.addResource(new DummyServerResource());
        environment.addHealthCheck(new JobProcessorHealthCheck("JobProcessor"));
    }


    public static void main(String[] args) throws Exception {
        args = new String[]{"server",args[0]};
        new LoaderAgentService().run(args);
    }

}
