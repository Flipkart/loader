package loader.monitor;

/**
 * Date : 28/12/2012
 * USer : nitinka
 */
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import loader.monitor.collector.CollectorThread;
import loader.monitor.config.ServerMonitoringConfig;
import loader.monitor.publisher.MetricPublisherThread;
import loader.monitor.resource.CollectorResource;
import loader.monitor.resource.OnDemandCollectorResource;
import loader.monitor.resource.PublishRequestResource;

import java.lang.reflect.InvocationTargetException;

public class MonitoringService extends Service<ServerMonitoringConfig> {

    @Override
    public void initialize(Bootstrap<ServerMonitoringConfig> bootstrap) {
        bootstrap.setName("loader-agent");
    }

    @Override
    public void run(ServerMonitoringConfig configuration, Environment environment) throws Exception {
        CollectorThread collectorThread = startCollectorThread(1000);
        MetricPublisherThread metricPublisherThread = startStartThread(1000);
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
        args = new String[]{"server", args[0]};
        new MonitoringService().run(args);
    }

}
