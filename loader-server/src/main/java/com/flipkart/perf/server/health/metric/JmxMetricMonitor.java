package com.flipkart.perf.server.health.metric;

import com.flipkart.perf.common.jmx.JMXConnection;
import com.sun.management.GarbageCollectorMXBean;
import sun.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/10/13
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class JmxMetricMonitor extends MetricMonitor{

    public JmxMetricMonitor(String name) {
        super(name);
    }

    @Override
    public ResourceMetric get() throws IOException, MalformedObjectNameException {
        MBeanServer server = ManagementFactory.createPlatformMBeanServer();
        JMXConnection jmxConnection = new JMXConnection(server);

        // Memory utilization
        MemoryMXBean memoryMXBean = jmxConnection.getMemoryMXBean();
        MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
        ResourceMetric resourceMetric = new ResourceMetric();
        resourceMetric.addMetrics(this.getName() + ".memory.heap.max", (double) memoryUsage.getMax());
        resourceMetric.addMetrics(this.getName() + ".memory.heap.committed", (double) memoryUsage.getCommitted());
        resourceMetric.addMetrics(this.getName() + ".memory.heap.used", (double) memoryUsage.getUsed());
        resourceMetric.addMetrics(this.getName() + ".memory.heap.percentage", memoryUsage.getUsed() * 100d / memoryUsage.getMax());

        // Load Average
        OperatingSystemMXBean osMXBean        = jmxConnection.getOperatingSystemMXBean();
        resourceMetric.addMetrics(this.getName() + ".os.loadAverage", osMXBean.getSystemLoadAverage());

        // Thread Info
        ThreadMXBean threadMXBean    = jmxConnection.getThreadMXBean();
        resourceMetric.addMetrics(this.getName() + ".threads.total", (double) threadMXBean.getTotalStartedThreadCount());
        resourceMetric.addMetrics(this.getName() + ".threads.live", (double) threadMXBean.getThreadCount());

        if(jmxConnection.getImplementationVersion().startsWith("1.6")) {
            int deadLockedThreadsCount      =   0;
            long[]  deadLockedThreads       =   threadMXBean.findDeadlockedThreads();
            if(deadLockedThreads    !=  null)
                deadLockedThreadsCount  =   deadLockedThreads.length;
            resourceMetric.addMetrics(this.getName() + ".thread.deadlocked", (double) deadLockedThreadsCount);
        }

        // GC info
        List<GarbageCollectorMXBean> gcPool	=	jmxConnection.getGCPoolMXBeans();
        for(GarbageCollectorMXBean	gc	:	gcPool) {
            resourceMetric.addMetrics(this.getName() + ".gc." + gc.getName() + ".count", (double) gc.getCollectionCount());
            resourceMetric.addMetrics(this.getName() + ".gc." + gc.getName() + ".time", (double) gc.getCollectionTime());
        }

        return resourceMetric;
    }
}
