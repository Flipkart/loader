package perf.sample;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.FilterBuilder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import perf.sample.config.SampleServiceConfiguration;
import perf.sample.resource.HelloResource;
import perf.sample.resource.MemoryHoggerResource;
import perf.sample.resource.SearchResource;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 2/7/13
 * Time: 6:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleService  extends Service<SampleServiceConfiguration> {
    public SampleService() {
    }

    @Override
    public void initialize(Bootstrap<SampleServiceConfiguration> bootstrap) {
        bootstrap.setName("sample-service");
    }

    @Override
    public void run(SampleServiceConfiguration configuration, Environment environment) throws Exception {
        environment.addResource(new HelloResource());
        environment.addResource(new SearchResource(configuration.getSearchPoolSize()));
        environment.addResource(new MemoryHoggerResource(configuration.getSearchPoolSize()));
    }

    public static void main(String[] args) throws Exception {
        args = new String[]{"server",args[0]};
        new SampleService().run(args);
    }
}
