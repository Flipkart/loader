package server.monitor.collector;

import com.open.perf.jmx.JVMInfo;
import com.sun.management.GarbageCollectorMXBean;
import server.monitor.domain.ResourceCollectionInstance;

import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.lang.management.*;
import java.util.*;

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
        JVMInfo jvmInfo = new JVMInfo(this.getParam("host").toString(),
                (Integer) this.getParam("port"));
        ResourceCollectionInstance collectionInstance = new ResourceCollectionInstance().
                setResourceName(this.getName());

        collectionInstance.addMetrics(getMemoryMetrics(jvmInfo));
        collectionInstance.addMetrics(getGCMetrics(jvmInfo));
        collectionInstance.addMetrics(getThreadsMetric(jvmInfo));
        collectionInstance.addMetrics(getClassLoadingMetrics(jvmInfo));
        collectionInstance.addMetrics(getOSMetrics(jvmInfo));
        jvmInfo.close();
        return collectionInstance.setTime(System.currentTimeMillis());
    }

    private Map<String, Double> getOSMetrics(JVMInfo jvmInfo) throws IOException {
        Map<String, Double> metrics = new HashMap<String, Double>();
        if(jvmInfo.getImplementationVersion().startsWith("1.6")) {
            OperatingSystemMXBean   osMXBean        = jvmInfo.getOperatingSystemMXBean();
            metrics.put(OS + "." + SYSTEM_AVERAGE_LOAD, osMXBean.getSystemLoadAverage());
        }

        return metrics;
    }

    private Map<String, Double> getClassLoadingMetrics(JVMInfo jvmInfo) throws IOException {
        Map<String, Double> metrics = new HashMap<String, Double>();
        ClassLoadingMXBean classLoadingMXBean  = jvmInfo.getClassLoadingMXBean();

        metrics.put(CLASS_LOADER + "." + CLASS_TOTAL_LOADED, (double) classLoadingMXBean.getTotalLoadedClassCount());
        metrics.put(CLASS_LOADER + "." + CLASS_CURRENTLY_LOADED, (double) classLoadingMXBean.getLoadedClassCount());
        metrics.put(CLASS_LOADER + "." + CLASS_TOTAL_UNLOADED, (double) classLoadingMXBean.getUnloadedClassCount());

        return metrics;
    }

    private Map<String, Double> getThreadsMetric(JVMInfo jvmInfo) throws IOException {
        Map<String, Double> metrics = new HashMap<String, Double>();
        ThreadMXBean threadMXBean    = jvmInfo.getThreadMXBean();

        metrics.put(THREAD + "." + THREADS_TOTAL, (double) threadMXBean.getTotalStartedThreadCount());
        metrics.put(THREAD + "." + THREADS_CURRENT, (double) threadMXBean.getThreadCount());

        if(jvmInfo.getImplementationVersion().startsWith("1.6")) {
            int deadLockedThreadsCount      =   0;
            long[]  deadLockedThreads       =   threadMXBean.findDeadlockedThreads();
            if(deadLockedThreads    !=  null)
                deadLockedThreadsCount  =   deadLockedThreads.length;

            metrics.put("thread." + THREADS_DEADLOCKED, (double) deadLockedThreadsCount);
        }

        return metrics;
    }

    private Map<String, Double> getGCMetrics(JVMInfo jvmInfo) throws MalformedObjectNameException, IOException {
        Map<String, Double> metrics = new HashMap<String, Double>();
        List<GarbageCollectorMXBean> gcPool	=	jvmInfo.getGCPoolMXBeans();
        for(GarbageCollectorMXBean	gc	:	gcPool) {
            metrics.put(GC + "." + gc.getName() + ".count", (double) gc.getCollectionCount());
            metrics.put(GC + "." + gc.getName() + ".time", (double) gc.getCollectionTime());
        }
        return metrics;
    }

    private Map<String, Double> getMemoryMetrics(JVMInfo jvmInfo) throws IOException, MalformedObjectNameException {
        Map<String, Double> metrics = new HashMap<String, Double>();

        MemoryMXBean memoryMXBean = jvmInfo.getMemoryMXBean();
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

        List<MemoryPoolMXBean> memoryPoolMXBeanList = jvmInfo.getMemoryPoolMXBeans();
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