package loader.monitor.collector.jmx;

import com.sun.management.GarbageCollectorMXBean;

import javax.management.MBeanServerDelegate;
import javax.management.MalformedObjectNameException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.*;
import java.util.*;

/**
 * 
 * @author NitinK.Agarwal@yahoo.com
 * Collects and archives various jvm jmx emitted variables.
 */
/*
public class JVMInfoDumpMonitor extends AbstractMonitor {

    public static final String  JVM_INFO                = "jvmInfo";
    public static final String  MEMORY_INFO             = "memoryInfo";
    public static final String  HEAP_MEMORY_INFO        = "heapMemoryInfo";
    public static final String  NON_HEAP_MEMORY_INFO    = "nonHeapMemoryInfo";
    public static final String  CLASS_LOADER_INFO       = "classLoaderInfo";
    public static final String  THREAD_INFO             = "threadInfo";
    public static final String  OS_INFO                 = "osInfo";
    public static final String  GC_INFO                 = "gcInfo";
    
    public static final String  MAX                     = "max";
    public static final String  PERCENTAGE              = "usagePercentage";
    public static final String  TIME                    = "time";
    public static final String  COMMITTED               = "committed";
    public static final String  USED                    = "used";
    public static final String  THEADS_TOTAL            = "threadsTotal";
    public static final String  THREADS_CURRENT         = "threadsCurrent";
    public static final String  THREADS_DEADLOCKED      = "threadsDeadlocked";
    public static final String  CLASS_TOTAL_LOADED      = "classTotalLoaded";
    public static final String  CLASS_TOTAL_UNLOADED    = "classTotalUnloaded";
    public static final String  CLASS_CURRENTLY_LOADED  = "classCurrentlyLoaded";
    public static final String  SYSTEM_AVERAGE_LOAD     = "systemAverageLoad";
    
    public static final String  LOG_FOLDER              = "logFolder";
    public static final String  VM_URLS                 = "vmURLs";
    public static final String  GATHER_LIST             = "gatherList";
    
    public static MBeanServerDelegate mBeanServerDelegate ;
    public static AlertQueue    alertQueue              ;

    static {
        mBeanServerDelegate =   new MBeanServerDelegate();
        alertQueue          =   AlertQueue.getAlertqueue();
    }

    public void execute(HashMap<String,String> params) throws MalformedObjectNameException, NullPointerException, IOException, JSONException{
        String  logFile     = params.get(LOG_FOLDER);
        File    logFolder   = new File(logFile);

        if(logFolder.exists() == false)
            if(logFolder.mkdirs() == false)
                throw new RuntimeException("Couldn't create "+logFile);
        
        Map<String,JSONObject> vmInfoJSONMap    = getJVMInfoJSONMap(params);
    
        String metaFile = logFile+File.separatorChar+"vmFiles.txt";
        BufferedWriter bwM   = new BufferedWriter(new FileWriter(metaFile));
        
        for(String vm : vmInfoJSONMap.keySet()) {
            String fileName     = logFile+File.separatorChar+vm.replace(":", "_")+".txt";
            bwM.write(vm+"="+fileName+"\n");
            bwM.flush();
            BufferedWriter bw   = new BufferedWriter(new FileWriter(fileName,true));
            JSONObject  jvmInfoJSONObject   = vmInfoJSONMap.get(vm);
            bw.write(jvmInfoJSONObject+"\n");
            bw.flush();
            bw.close();

        }            
        bwM.close();
    }

    public static Map<String,JSONObject> getJVMInfoJSONMap(HashMap<String,String> params) throws IOException, JSONException, MalformedObjectNameException, NullPointerException {
        String      vmURLsStr           = params.get(VM_URLS);
        
        String[]    vmURLs              = new String[]{};
        if(vmURLsStr!=null && vmURLsStr.equals("") ==false)
            vmURLs                      = vmURLsStr.split(",");
        else
            throw new RuntimeException("No value for vmURLs!!!");
        
        String      gatherListStr           = params.get(GATHER_LIST);// What information do you want to gather
        String[]    gatherListArr           = new String[]{};
        ArrayList<String> gatherList    = new ArrayList<String>();

        if(gatherListStr!=null && gatherListStr.equals("") ==false){
            gatherListArr                      = gatherListStr.split(",");
            for(int gatherI=0; gatherI<gatherListArr.length;gatherI++)
                gatherList.add(gatherListArr[gatherI]);
        }    
        else
        {
            gatherList.add(JVMInfoDumpMonitor.MEMORY_INFO);
            gatherList.add(JVMInfoDumpMonitor.THREAD_INFO);
            gatherList.add(JVMInfoDumpMonitor.OS_INFO);
            gatherList.add(JVMInfoDumpMonitor.CLASS_LOADER_INFO);
            gatherList.add(JVMInfoDumpMonitor.GC_INFO);
        }
        
        Map<String, JSONObject> jsonObjectMap   = new HashMap<String,JSONObject>();
        
        for(String vmURL: vmURLs) {
            JVMInfo         jvmInfo             = new JVMInfo(vmURL.split(":")[0],Integer.parseInt(vmURL.split(":")[1]));
            JSONObject      jvmInfoJSONObject   = new JSONObject();

            long currentTime                    = System.currentTimeMillis();  
            Date date                           = new Date(currentTime);
            
            jvmInfoJSONObject.put(JVMInfoDumpMonitor.TIME, currentTime);

            // Preparing JSON Object for Memory Utilization of All Type
            if(gatherList.contains(JVMInfoDumpMonitor.MEMORY_INFO)) {
                JSONObject      memoryJSONObject    = new JSONObject();
                MemoryMXBean    memoryMXBean        = jvmInfo.getMemoryMXBean();
                MemoryUsage     memoryUsage         = memoryMXBean.getHeapMemoryUsage();
                
                double  usagePer                    = memoryUsage.getUsed()*100/memoryUsage.getMax();
                memoryJSONObject.put(JVMInfoDumpMonitor.HEAP_MEMORY_INFO, new JSONObject()
                                                        .put(JVMInfoDumpMonitor.MAX, memoryUsage.getMax())
                                                        .put(JVMInfoDumpMonitor.COMMITTED, memoryUsage.getCommitted())
                                                        .put(JVMInfoDumpMonitor.USED, memoryUsage.getUsed())
                                                        .put(JVMInfoDumpMonitor.PERCENTAGE, usagePer));
                
                String alertName                    =   vmURL+"-"+ JVMInfoDumpMonitor.MEMORY_INFO+"."+ JVMInfoDumpMonitor.HEAP_MEMORY_INFO+"."+ JVMInfoDumpMonitor.PERCENTAGE;
                alertQueue.addAlert(new AlertData(alertName, usagePer, date));
                
                
                memoryUsage                         = memoryMXBean.getNonHeapMemoryUsage();
                usagePer                            = memoryUsage.getUsed()*100/memoryUsage.getMax();
                memoryJSONObject.put(JVMInfoDumpMonitor.NON_HEAP_MEMORY_INFO, new JSONObject()
                                                        .put(JVMInfoDumpMonitor.MAX, memoryUsage.getMax())
                                                        .put(JVMInfoDumpMonitor.COMMITTED, memoryUsage.getCommitted())
                                                        .put(JVMInfoDumpMonitor.USED, memoryUsage.getUsed())
                                                        .put(JVMInfoDumpMonitor.PERCENTAGE, usagePer));
                alertName                           =   vmURL+"-"+ JVMInfoDumpMonitor.MEMORY_INFO+"."+ JVMInfoDumpMonitor.NON_HEAP_MEMORY_INFO+"."+ JVMInfoDumpMonitor.PERCENTAGE;
                alertQueue.addAlert(new AlertData(alertName, usagePer, date));
                
                
                List<MemoryPoolMXBean> memoryPoolMXBealList = jvmInfo.getMemoryPoolMXBeans();
                for(MemoryPoolMXBean memoryBean : memoryPoolMXBealList) {
                    
                    memoryUsage                     = memoryBean.getUsage();
                    usagePer                        = memoryUsage.getUsed()*100/memoryUsage.getMax();

                    memoryJSONObject.put(memoryBean.getName().replace(" " , "_"), new JSONObject()
                                                        .put(JVMInfoDumpMonitor.MAX, memoryUsage.getMax())
                                                        .put(JVMInfoDumpMonitor.COMMITTED, memoryUsage.getCommitted())
                                                        .put(JVMInfoDumpMonitor.USED, memoryUsage.getUsed())
                                                        .put(JVMInfoDumpMonitor.PERCENTAGE, usagePer));

                    alertName                       =   vmURL+"-"+ JVMInfoDumpMonitor.MEMORY_INFO+"."+memoryBean.getName().replace(" " , "_")+"."+ JVMInfoDumpMonitor.PERCENTAGE;
                    alertQueue.addAlert(new AlertData(alertName, usagePer, date));

                }
                jvmInfoJSONObject.put(JVMInfoDumpMonitor.MEMORY_INFO, memoryJSONObject);
            }
            
            if(gatherList.contains(JVMInfoDumpMonitor.GC_INFO)) {
                
            	JSONObject	gcJSONObject			=	new JSONObject();
            	List<GarbageCollectorMXBean> gcPool	=	jvmInfo.getGCPoolMXBeans();
            	for(GarbageCollectorMXBean	gc	:	gcPool) {
            		gcJSONObject.put(gc.getName().replace("" , "_"), new JSONObject().put("count", gc.getCollectionCount())
            																		.put("time", gc.getCollectionTime()));
            	}
                jvmInfoJSONObject.put(JVMInfoDumpMonitor.GC_INFO, gcJSONObject);
            }
            
            // Preparing minimal Thread Info
            if(gatherList.contains(JVMInfoDumpMonitor.THREAD_INFO)) {
                ThreadMXBean    threadMXBean    = jvmInfo.getThreadMXBean();
                JSONObject threadInfo   =   new JSONObject();
                threadInfo.put(JVMInfoDumpMonitor.THEADS_TOTAL, threadMXBean.getTotalStartedThreadCount())
                                                        .put(JVMInfoDumpMonitor.THREADS_CURRENT, threadMXBean.getThreadCount());
                String alertName                       =   vmURL+"-"+ JVMInfoDumpMonitor.THREAD_INFO+"."+ JVMInfoDumpMonitor.THREADS_CURRENT;
                alertQueue.addAlert(new AlertData(alertName, threadMXBean.getThreadCount(), date));
                
                
                if(jvmInfo.getImplementationVersion().startsWith("1.6")) {
                    alertName                       =   vmURL+"-"+ JVMInfoDumpMonitor.THREAD_INFO+"."+ JVMInfoDumpMonitor.THREADS_DEADLOCKED;
                    int deadLockedThreadsCount      =   0;
                    long[]  deadLockedThreads       =   threadMXBean.findDeadlockedThreads();
                    if(deadLockedThreads    !=  null)
                        deadLockedThreadsCount  =   deadLockedThreads.length;   
                    
                    if(deadLockedThreadsCount>0)
                        alertQueue.addAlert(new TrueAlertData(alertName, deadLockedThreadsCount, date));

                    threadInfo.put(JVMInfoDumpMonitor.THREADS_DEADLOCKED, threadMXBean.findDeadlockedThreads());
                }
                jvmInfoJSONObject.put(JVMInfoDumpMonitor.THREAD_INFO, threadInfo);

            }
                
            // Preparing ClassLoading Info
            if(gatherList.contains(JVMInfoDumpMonitor.CLASS_LOADER_INFO)) {
                ClassLoadingMXBean  classLoadingMXBean  = jvmInfo.getClassLoadingMXBean();
                jvmInfoJSONObject.put(JVMInfoDumpMonitor.CLASS_LOADER_INFO, new JSONObject()
                                                        .put(JVMInfoDumpMonitor.CLASS_TOTAL_LOADED, classLoadingMXBean.getTotalLoadedClassCount())
                                                        .put(JVMInfoDumpMonitor.CLASS_CURRENTLY_LOADED, classLoadingMXBean.getLoadedClassCount())
                                                        .put(JVMInfoDumpMonitor.CLASS_TOTAL_UNLOADED, classLoadingMXBean.getUnloadedClassCount()));

            }

            // Preparing OS Info
            if(jvmInfo.getImplementationVersion().startsWith("1.6")) {
                if(gatherList.contains(JVMInfoDumpMonitor.OS_INFO)) {
                    OperatingSystemMXBean   osMXBean        = jvmInfo.getOperatingSystemMXBean();
                    osMXBean.getSystemLoadAverage();
                    jvmInfoJSONObject.put(JVMInfoDumpMonitor.OS_INFO, new JSONObject()
                                                            .put(JVMInfoDumpMonitor.SYSTEM_AVERAGE_LOAD, osMXBean.getSystemLoadAverage())
                                                            );

                    String alertName                       =   vmURL+"-"+ JVMInfoDumpMonitor.OS_INFO+"."+ JVMInfoDumpMonitor.SYSTEM_AVERAGE_LOAD;
                    alertQueue.addAlert(new AlertData(alertName, osMXBean.getSystemLoadAverage(),date));
                }
            }
            jsonObjectMap.put(vmURL, jvmInfoJSONObject);
            jvmInfo.close();
        }
        
        return jsonObjectMap;
    }
    
    public static JSONObject getJVMInfoJSONObject(HashMap<String,String> params) throws IOException, JSONException, MalformedObjectNameException, NullPointerException {
        
        Map<String, JSONObject> vmInfoMap = getJVMInfoJSONMap(params);
        JSONObject          allJVMsJSONObject   = new JSONObject();
        for(String vm : vmInfoMap.keySet()) {
            allJVMsJSONObject.put(vm, vmInfoMap.get(vm));
        }
        
        return new JSONObject().put(JVMInfoDumpMonitor.JVM_INFO, allJVMsJSONObject);
    }

    public static void main(String[] arg){
        System.out.println(new Date());
    }
    
}
*/
