package server.monitor.collector;

import com.open.perf.jmx.JMXConnection;
import com.sun.management.GarbageCollectorMXBean;
import server.monitor.domain.ResourceCollectionInstance;

import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.lang.management.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 25/1/13
 * Time: 4:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class JMXCollector extends BaseCollector {
    public static final String  MEMORY             = "memory";
    public static final String  HEAP_MEMORY        = "heapMemory";
    public static final String  NON_HEAP_MEMORY    = "nonHeapMemory";
    public static final String  CLASS_LOADER       = "classLoader";
    public static final String  THREAD             = "thread";
    public static final String  OS                 = "os";
    public static final String  GC                 = "gc";

    public static final String  MAX                     = "max";
    public static final String  PERCENTAGE              = "usagePercentage";
    public static final String  TIME                    = "time";
    public static final String  COMMITTED               = "committed";
    public static final String  USED                    = "used";
    public static final String  THREADS_TOTAL            = "total";
    public static final String  THREADS_CURRENT         = "current";
    public static final String  THREADS_DEADLOCKED      = "deadlocked";
    public static final String  CLASS_TOTAL_LOADED      = "loaded";
    public static final String  CLASS_TOTAL_UNLOADED    = "unloaded";
    public static final String  CLASS_CURRENTLY_LOADED  = "currentlyLoaded";
    public static final String  SYSTEM_AVERAGE_LOAD     = "systemAverageLoad";

    public JMXCollector(String name, Map<String, Object> params, int interval) {
        super(name, params, interval);
    }

    @Override
    public boolean supported() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ResourceCollectionInstance collect() throws Exception {
        JMXConnection jmxConnection = null;

        if(this.getParam("host") != null && this.getParam("port") != null) {
            jmxConnection = new JMXConnection(this.getParam("host").toString(),
                    (Integer) this.getParam("port"));
        }
        else if(this.getParam("jmxConnectorAddress") != null) {
            jmxConnection = new JMXConnection(this.getParam("jmxConnectorAddress").toString());
        }

        ResourceCollectionInstance collectionInstance = new ResourceCollectionInstance().
                setResourceName(this.getName());

        collectionInstance.addMetrics(getMemoryMetrics(jmxConnection));
        collectionInstance.addMetrics(getGCMetrics(jmxConnection));
        collectionInstance.addMetrics(getThreadsMetric(jmxConnection));
        collectionInstance.addMetrics(getClassLoadingMetrics(jmxConnection));
        collectionInstance.addMetrics(getOSMetrics(jmxConnection));
        jmxConnection.close();
        return collectionInstance.setTime(System.currentTimeMillis());
    }

    private Map<String, Double> getOSMetrics(JMXConnection JMXConnection) throws IOException {
        Map<String, Double> metrics = new HashMap<String, Double>();
        if(JMXConnection.getImplementationVersion().startsWith("1.6")) {
            OperatingSystemMXBean   osMXBean        = JMXConnection.getOperatingSystemMXBean();
            metrics.put(OS + "." + SYSTEM_AVERAGE_LOAD, osMXBean.getSystemLoadAverage());
        }

        return metrics;
    }

    private Map<String, Double> getClassLoadingMetrics(JMXConnection JMXConnection) throws IOException {
        Map<String, Double> metrics = new HashMap<String, Double>();
        ClassLoadingMXBean classLoadingMXBean  = JMXConnection.getClassLoadingMXBean();

        metrics.put(CLASS_LOADER + "." + CLASS_TOTAL_LOADED, (double) classLoadingMXBean.getTotalLoadedClassCount());
        metrics.put(CLASS_LOADER + "." + CLASS_CURRENTLY_LOADED, (double) classLoadingMXBean.getLoadedClassCount());
        metrics.put(CLASS_LOADER + "." + CLASS_TOTAL_UNLOADED, (double) classLoadingMXBean.getUnloadedClassCount());

        return metrics;
    }

    private Map<String, Double> getThreadsMetric(JMXConnection JMXConnection) throws IOException {
        Map<String, Double> metrics = new HashMap<String, Double>();
        ThreadMXBean threadMXBean    = JMXConnection.getThreadMXBean();

        metrics.put(THREAD + "." + THREADS_TOTAL, (double) threadMXBean.getTotalStartedThreadCount());
        metrics.put(THREAD + "." + THREADS_CURRENT, (double) threadMXBean.getThreadCount());

        if(JMXConnection.getImplementationVersion().startsWith("1.6")) {
            int deadLockedThreadsCount      =   0;
            long[]  deadLockedThreads       =   threadMXBean.findDeadlockedThreads();
            if(deadLockedThreads    !=  null)
                deadLockedThreadsCount  =   deadLockedThreads.length;

            metrics.put("thread." + THREADS_DEADLOCKED, (double) deadLockedThreadsCount);
        }

        return metrics;
    }

    private Map<String, Double> getGCMetrics(JMXConnection JMXConnection) throws MalformedObjectNameException, IOException {
        Map<String, Double> metrics = new HashMap<String, Double>();
        List<GarbageCollectorMXBean> gcPool	=	JMXConnection.getGCPoolMXBeans();
        for(GarbageCollectorMXBean	gc	:	gcPool) {
            metrics.put(GC + "." + gc.getName() + ".count", (double) gc.getCollectionCount());
            metrics.put(GC + "." + gc.getName() + ".time", (double) gc.getCollectionTime());
        }
        return metrics;
    }

    private Map<String, Double> getMemoryMetrics(JMXConnection JMXConnection) throws IOException, MalformedObjectNameException {
        Map<String, Double> metrics = new HashMap<String, Double>();

        MemoryMXBean memoryMXBean = JMXConnection.getMemoryMXBean();
        MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();

        metrics.put(MEMORY + "." + HEAP_MEMORY + "." + MAX, (double) memoryUsage.getMax());
        metrics.put(MEMORY + "." + HEAP_MEMORY + "." + COMMITTED, (double) memoryUsage.getCommitted());
        metrics.put(MEMORY + "." + HEAP_MEMORY + "." + USED, (double) memoryUsage.getUsed());
        metrics.put(MEMORY + "." + HEAP_MEMORY + "." + PERCENTAGE, memoryUsage.getUsed() * 100d / memoryUsage.getMax());
 
        memoryUsage                         = memoryMXBean.getNonHeapMemoryUsage();
        metrics.put(MEMORY + "." + NON_HEAP_MEMORY + "." + MAX, (double) memoryUsage.getMax());
        metrics.put(MEMORY + "." + NON_HEAP_MEMORY + "." + COMMITTED, (double) memoryUsage.getCommitted());
        metrics.put(MEMORY + "." + NON_HEAP_MEMORY + "." + USED, (double) memoryUsage.getUsed());
        metrics.put(MEMORY + "." + NON_HEAP_MEMORY + "." + PERCENTAGE, memoryUsage.getUsed() * 100d / memoryUsage.getMax());

        List<MemoryPoolMXBean> memoryPoolMXBeanList = JMXConnection.getMemoryPoolMXBeans();
        for(MemoryPoolMXBean memoryBean : memoryPoolMXBeanList) {
            memoryUsage  = memoryMXBean.getNonHeapMemoryUsage();
            metrics.put(MEMORY + "." + memoryBean.getName().replace(" ", "_") + "." + MAX, (double) memoryUsage.getMax());
            metrics.put(MEMORY + "." + memoryBean.getName().replace(" ", "_") + "." + COMMITTED, (double) memoryUsage.getCommitted());
            metrics.put(MEMORY + "." + memoryBean.getName().replace(" ", "_") + "." + USED, (double) memoryUsage.getUsed());
            metrics.put(MEMORY + "." + memoryBean.getName().replace(" ", "_") + "." + PERCENTAGE, memoryUsage.getUsed() * 100d / memoryUsage.getMax());
        }

        return metrics;
    }

}
