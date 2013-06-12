package server.monitor;

/**
 * Date : 28/12/2012
 * USer : nitinka
 */

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jetty.servlets.CrossOriginFilter;

import server.monitor.collector.CollectorThread;
import server.monitor.collector.jmx.MonitorLocalJavaProcesses;
import server.monitor.config.ServerMonitoringConfig;
import server.monitor.publisher.MetricPublisherThread;
import server.monitor.resource.CollectorResource;
import server.monitor.resource.OnDemandCollectorResource;
import server.monitor.resource.PublishRequestResource;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.FilterBuilder;

public class MonitoringService extends Service<ServerMonitoringConfig> {

    @Override
    public void initialize(Bootstrap<ServerMonitoringConfig> bootstrap) {
        bootstrap.setName("monitoring-service");
    }

    @Override
    public void run(ServerMonitoringConfig configuration, Environment environment) throws Exception {
    	FilterBuilder filterConfig = environment.addFilter(CrossOriginFilter.class, "/*");
        filterConfig.setInitParam(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM, String.valueOf(60*60*24));
        CollectorThread collectorThread = startCollectorThread(1000);
        MetricPublisherThread metricPublisherThread = startStartThread(1000);
        new MonitorLocalJavaProcesses(60000, collectorThread).start();
        environment.addResource(new CollectorResource());
        environment.addResource(new PublishRequestResource(metricPublisherThread));
        environment.addResource(new OnDemandCollectorResource(configuration.getOnDemandCollectors(),
                collectorThread));
    }

    private MetricPublisherThread startStartThread(int publisherCheckInterval) {
        MetricPublisherThread metricPublisherThread = new MetricPublisherThread(publisherCheckInterval);
        metricPublisherThread.start();
        return metricPublisherThread;
    }

    private CollectorThread startCollectorThread(int collectionCheckInterval) throws InvocationTargetException,
            ClassNotFoundException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException {

        CollectorThread collectorThread = new CollectorThread(collectionCheckInterval);
        collectorThread.start();
        return collectorThread;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new File(".").getAbsolutePath());
        args = new String[]{"server", args[0]};
        new MonitoringService().run(args);
    }

}
