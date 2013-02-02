package loader.monitor.collector.jmx;

import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.lang.management.ManagementFactory.*;

/**
 * 
 * @author NitinK.Agarwal@yahoo.com
 *
 */
public class JVMInfo
{
    private MBeanServerConnection server;
    private String hostName;
    private int port;
    private JMXConnector   jmxc;
    
    public JVMInfo(String hostName, int port) throws IOException {
        this.hostName       =   hostName;
        this.port           =   port;
  
        // Create an RMI connector client and connect it to
        // the RMI connector server
        String urlPath = "/jndi/rmi://" + hostName + ":" + port + "/jmxrmi";
        
        //Connect to a JMX agent of a given URL. 
        JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);
        //System.out.println(url.toString());    
        this.jmxc = JMXConnectorFactory.connect(url);
        this.server = jmxc.getMBeanServerConnection();
        
    }
    
    public List<MemoryPoolMXBean> getMemoryPoolMXBeans() throws MalformedObjectNameException, NullPointerException, IOException {
        List<MemoryPoolMXBean> memoryPoolMXBeans    = new ArrayList<MemoryPoolMXBean>();
        
        // ObjectName Representing the MXBean
        ObjectName poolName                         = new ObjectName(MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",*");

        // Getting set of all the Objects that represents set of MXBeans
        Set<ObjectName> mbeans                      = this.server.queryNames(poolName, null);

        if (mbeans != null) 
        {
            Iterator<ObjectName> iterator = mbeans.iterator();
            while (iterator.hasNext()) 
            {
                ObjectName objName = (ObjectName) iterator.next();
                // Getting the MX Bean
                MemoryPoolMXBean p = newPlatformMXBeanProxy(server,objName.getCanonicalName(),MemoryPoolMXBean.class);
                memoryPoolMXBeans.add(p);
            }
        }
        return memoryPoolMXBeans; 
    }
    
    public List<GarbageCollectorMXBean> getGCPoolMXBeans() throws MalformedObjectNameException, NullPointerException, IOException {
        List<GarbageCollectorMXBean> gcPoolMXBeans  = new ArrayList<GarbageCollectorMXBean>();
            
        // ObjectName Representing the MXBean
        ObjectName  poolName                        = new ObjectName(GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE+",*");

        // Getting set of all the Objects that represents set of MXBeans
        Set<ObjectName> mbeans                      = this.server.queryNames(poolName, null);

        if (mbeans != null) 
        {
            Iterator<ObjectName> iterator = mbeans.iterator();
            while (iterator.hasNext()) 
            {
                ObjectName objName = (ObjectName) iterator.next();
                // Getting the MX Bean
                GarbageCollectorMXBean p = newPlatformMXBeanProxy(server,objName.getCanonicalName(),GarbageCollectorMXBean.class);
                gcPoolMXBeans.add(p);
            }
        }
        return gcPoolMXBeans; 
    }

    public ThreadMXBean getThreadMXBean() throws IOException {
         return ManagementFactory.newPlatformMXBeanProxy(server,ManagementFactory.THREAD_MXBEAN_NAME,ThreadMXBean.class);
    }

    public RuntimeMXBean getRuntimeMXBean() throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(server,ManagementFactory.RUNTIME_MXBEAN_NAME,RuntimeMXBean.class);
    }

    public MemoryMXBean getMemoryMXBean() throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(server,ManagementFactory.MEMORY_MXBEAN_NAME,MemoryMXBean.class);
    }

    public ClassLoadingMXBean getClassLoadingMXBean() throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(server,ManagementFactory.CLASS_LOADING_MXBEAN_NAME,ClassLoadingMXBean.class);
    }

    public CompilationMXBean getCompilationMXBean() throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(server,ManagementFactory.COMPILATION_MXBEAN_NAME,CompilationMXBean.class);
    }

    public OperatingSystemMXBean getOperatingSystemMXBean() throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(server,ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME,OperatingSystemMXBean.class);
    }
    
    public String getHostName() {
        return this.hostName;
    }
    
    public int getPort() {
        return this.port;
    }

    public MBeanServerConnection getMXBeanServerConnection() {
        return this.server;
    }
        
    public MBeanServerDelegateMBean getMBeanServerDelegate() throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(server, "JMImplementation:type=MBeanServerDelegate", MBeanServerDelegateMBean.class);
    }
    
    public String getImplementationVersion() throws IOException {
        return getMBeanServerDelegate().getImplementationVersion();
    }
    
    public HotSpotDiagnosticMXBean getHotSpotDiagnosticMXBean() throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
    }
    
    public void getHeapDump(String outputFile, boolean onlyLiveObjects) throws IOException {
        getHotSpotDiagnosticMXBean().dumpHeap(outputFile, onlyLiveObjects);
    }
    
    public List<VMOption> getDiagnosticOptions() throws IOException {
        return getHotSpotDiagnosticMXBean().getDiagnosticOptions();
    }

    public void close() throws IOException {
        this.jmxc.close();
    }

    public MBeanServerConnection getServer() {
        return server;
    }

    public JMXConnector getJmxc() {
        return jmxc;
    }

    public static void main(String[] arg) throws IOException, MalformedObjectNameException, NullPointerException, InterruptedException, IntrospectionException, InstanceNotFoundException, ReflectionException, AttributeNotFoundException, MBeanException {
    	JVMInfo         jvmInfo             = new JVMInfo("perf-digital-cms-db1.nm.flipkart.com",7199);
        MBeanServerConnection connection = jvmInfo.getMXBeanServerConnection();
        ObjectName objectName = new ObjectName("org.apache.cassandra.db:type=Caches");
        MBeanInfo beanInfo  =connection.getMBeanInfo(objectName);
        String[] attributes = new String[beanInfo.getAttributes().length];
/*
        for(beanInfo.l)


        AttributeList attributeList = connection.getAttributes(objectName, attributes);

        for(Attribute attribute : attributeList.asList()) {
            System.out.println(attribute);
        }
*/
        jvmInfo.close();
/*
    	while(true) {
        	List<GarbageCollectorMXBean> gcPool	=	jvmInfo.getGCPoolMXBeans();
        	for(GarbageCollectorMXBean	gc	:	gcPool) {
        		System.out.println(gc.getName()+" "+gc.getCollectionCount()+" "+gc.getCollectionTime());
        	}
        	System.out.println();
        	Thread.sleep(1000);
    	}
*/

    }
}
