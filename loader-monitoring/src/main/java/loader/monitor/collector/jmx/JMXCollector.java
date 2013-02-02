package loader.monitor.collector.jmx;

import com.sun.management.GarbageCollectorMXBean;
import loader.monitor.collector.BaseCollector;
import loader.monitor.collector.ResourceCollectionInstance;
import loader.monitor.domain.Metric;

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
    public static final String  JVM                = "jvm";
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

    private List<Metric> getOSMetrics(JVMInfo jvmInfo) throws IOException {
        List<Metric> metrics = new ArrayList<Metric>();
        if(jvmInfo.getImplementationVersion().startsWith("1.6")) {
            OperatingSystemMXBean   osMXBean        = jvmInfo.getOperatingSystemMXBean();
            metrics.add(new Metric().setName(OS+"." + SYSTEM_AVERAGE_LOAD).setValue((double) osMXBean.getSystemLoadAverage()));
        }

        return metrics;
    }

    private List<Metric> getClassLoadingMetrics(JVMInfo jvmInfo) throws IOException {
        List<Metric> metrics = new ArrayList<Metric>();
        ClassLoadingMXBean classLoadingMXBean  = jvmInfo.getClassLoadingMXBean();

        metrics.add(new Metric().setName(CLASS_LOADER+"." + CLASS_TOTAL_LOADED).setValue((double) classLoadingMXBean.getTotalLoadedClassCount()));
        metrics.add(new Metric().setName(CLASS_LOADER+"." + CLASS_CURRENTLY_LOADED).setValue((double) classLoadingMXBean.getLoadedClassCount()));
        metrics.add(new Metric().setName(CLASS_LOADER+"." + CLASS_TOTAL_UNLOADED).setValue((double) classLoadingMXBean.getUnloadedClassCount()));

        return metrics;
    }

    private List<Metric> getThreadsMetric(JVMInfo jvmInfo) throws IOException {
        List<Metric> metrics = new ArrayList<Metric>();
        ThreadMXBean threadMXBean    = jvmInfo.getThreadMXBean();

        metrics.add(new Metric().setName(THREAD + "." + THREADS_TOTAL).setValue((double) threadMXBean.getTotalStartedThreadCount()));
        metrics.add(new Metric().setName(THREAD + "." + THREADS_CURRENT).setValue((double) threadMXBean.getThreadCount()));

        if(jvmInfo.getImplementationVersion().startsWith("1.6")) {
            int deadLockedThreadsCount      =   0;
            long[]  deadLockedThreads       =   threadMXBean.findDeadlockedThreads();
            if(deadLockedThreads    !=  null)
                deadLockedThreadsCount  =   deadLockedThreads.length;

            metrics.add(new Metric().setName("thread." + THREADS_DEADLOCKED).setValue((double) deadLockedThreadsCount));
        }

        return metrics;
    }

    private List<Metric> getGCMetrics(JVMInfo jvmInfo) throws MalformedObjectNameException, IOException {
        List<Metric> metrics = new ArrayList<Metric>();
        List<GarbageCollectorMXBean> gcPool	=	jvmInfo.getGCPoolMXBeans();
        for(GarbageCollectorMXBean	gc	:	gcPool) {
            metrics.add(new Metric().setName(GC + "." + gc.getName() + ".count").setValue((double) gc.getCollectionCount()));
            metrics.add(new Metric().setName(GC + "." + gc.getName() + ".time").setValue((double) gc.getCollectionTime()));
        }
        return metrics;
    }

    private List<Metric> getMemoryMetrics(JVMInfo jvmInfo) throws IOException, MalformedObjectNameException {
        List<Metric> metrics = new ArrayList<Metric>();

        MemoryMXBean memoryMXBean = jvmInfo.getMemoryMXBean();
        MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();

        metrics.add(new Metric().setName("memory." + HEAP_MEMORY + "." + MAX).setValue((double) memoryUsage.getMax()));
        metrics.add(new Metric().setName("memory."+HEAP_MEMORY+"."+COMMITTED).setValue((double)memoryUsage.getCommitted()));
        metrics.add(new Metric().setName("memory."+HEAP_MEMORY+"."+USED).setValue((double)memoryUsage.getUsed()));
        metrics.add(new Metric().setName("memory."+HEAP_MEMORY+"."+PERCENTAGE).setValue(memoryUsage.getUsed()*100d/memoryUsage.getMax()));
 
        memoryUsage                         = memoryMXBean.getNonHeapMemoryUsage();
        metrics.add(new Metric().setName("memory."+NON_HEAP_MEMORY+"."+MAX).setValue((double)memoryUsage.getMax()));
        metrics.add(new Metric().setName("memory."+NON_HEAP_MEMORY+"."+COMMITTED).setValue((double)memoryUsage.getCommitted()));
        metrics.add(new Metric().setName("memory."+NON_HEAP_MEMORY+"."+USED).setValue((double)memoryUsage.getUsed()));
        metrics.add(new Metric().setName("memory."+NON_HEAP_MEMORY+"."+PERCENTAGE).setValue(memoryUsage.getUsed()*100d/memoryUsage.getMax()));

        List<MemoryPoolMXBean> memoryPoolMXBealList = jvmInfo.getMemoryPoolMXBeans();
        for(MemoryPoolMXBean memoryBean : memoryPoolMXBealList) {
            memoryUsage = memoryBean.getUsage();
            memoryUsage  = memoryMXBean.getNonHeapMemoryUsage();
            metrics.add(new Metric().setName("memory."+memoryBean.getName().replace(" " , "_")+"."+MAX).setValue((double)memoryUsage.getMax()));
            metrics.add(new Metric().setName("memory."+memoryBean.getName().replace(" " , "_")+"."+COMMITTED).setValue((double)memoryUsage.getCommitted()));
            metrics.add(new Metric().setName("memory."+memoryBean.getName().replace(" " , "_")+"."+USED).setValue((double)memoryUsage.getUsed()));
            metrics.add(new Metric().setName("memory." + memoryBean.getName().replace(" ", "_") + "." + PERCENTAGE).setValue(memoryUsage.getUsed() * 100d / memoryUsage.getMax()));

        }

        return metrics;
    }

}
