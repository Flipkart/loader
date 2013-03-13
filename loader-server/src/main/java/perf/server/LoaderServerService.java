package perf.server;

/**
 * Date : 28/12/2012
 * USer : nitinka
 */
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import perf.server.cache.LibCache;
import perf.server.config.LoaderServerConfiguration;
import perf.server.daemon.CounterCruncherThread;
import perf.server.resource.AgentResource;
import perf.server.resource.DeployLibResource;
import perf.server.resource.JobResource;

public class LoaderServerService extends Service<LoaderServerConfiguration> {

    @Override
    public void initialize(Bootstrap<LoaderServerConfiguration> bootstrap) {
        bootstrap.setName("loader-server");
    }

    @Override
    public void run(LoaderServerConfiguration configuration, Environment environment) throws Exception {
        environment.addProvider(com.sun.jersey.multipart.impl.MultiPartReaderServerSide.class);
        LibCache.initialize(configuration.getLibStorageFSConfig());
        //CounterCruncherThread.initialize(configuration.getJobFSConfig(), 10000).start();

        environment.addResource(new DeployLibResource(configuration.getLibStorageFSConfig()));
        environment.addResource(new AgentResource(configuration.getAgentConfig()));
        environment.addResource(new JobResource(configuration.getAgentConfig(),
                configuration.getMonitoringAgentConfig(),
                configuration.getJobFSConfig()));
    }


    public static void main(String[] args) throws Exception {
        args = new String[]{"server",args[0]};
        new LoaderServerService().run(args);
    }

}
