package server.monitor.collector.jmx;

import com.open.perf.util.Clock;
import com.sun.tools.attach.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.monitor.collector.BaseCollector;
import server.monitor.collector.CollectorThread;
import server.monitor.collector.JMXCollector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 15/5/13
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class MonitorLocalJavaProcesses extends Thread{
    private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";
    private static Logger logger = LoggerFactory.getLogger(MonitorLocalJavaProcesses.class);
    private final int searchInterval;
    private CollectorThread collectorThread;

    public MonitorLocalJavaProcesses(final int searchInterval, CollectorThread collectorThread) {
        this.searchInterval = searchInterval;
        this.collectorThread = collectorThread;
    }

    public void run() {
        Map<VirtualMachineDescriptor, BaseCollector> oldJVMs = new HashMap<VirtualMachineDescriptor, BaseCollector>();
        while(true) {
            long startTime = Clock.milliTick();
            List<VirtualMachineDescriptor> newJVMs = VirtualMachine.list();
            System.out.println("Time To find VMs :"+(Clock.milliTick() - startTime));
            List<VirtualMachineDescriptor> oldJVMsToRemove = new ArrayList<VirtualMachineDescriptor>();
            for(VirtualMachineDescriptor oldJVM : oldJVMs.keySet()) {
                if(!newJVMs.contains(oldJVM)) {
                    oldJVMsToRemove.add(oldJVM);
                }
            }

            // Code to Remove old jvms from onDemand monitoring
            for(VirtualMachineDescriptor oldJMVToRemove : oldJVMsToRemove) {
                BaseCollector collectorToRemove = oldJVMs.remove(oldJMVToRemove);
                this.collectorThread.stopCollector(collectorToRemove);

            }

            // Any new jvms should also be added to monitoring
            for(VirtualMachineDescriptor newJVM : newJVMs) {

                logger.debug("Found JVM :"+newJVM.displayName());
                if(newJVM.displayName().trim().equals("")) {
                    logger.info("JVM Name is Empty. Not instrumenting");
                    continue;
                }
                if(!oldJVMs.containsKey(newJVM) && !newJVM.displayName().contains("server.monitor.MonitoringService")) {
                    logger.info("Monitoring JVM :"+newJVM.displayName());
                    try {
                        String jmcConnectorAddress = buildJMXConnectorAddress(newJVM);
                        System.out.println("Time To Build JMX Connector:"+(Clock.milliTick() - startTime));
                        if(jmcConnectorAddress != null) {
                            //Add on demand Monitor

                            Map<String,Object> params = new HashMap<String, Object>();
                            params.put("jmxConnectorAddress", jmcConnectorAddress);

                            BaseCollector collector = new JMXCollector("jmx-"+newJVM.displayName().split(" ")[0],params, 60000);
                            this.collectorThread.startCollector(collector);
                            oldJVMs.put(newJVM, collector);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (AttachNotSupportedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (AgentLoadException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (AgentInitializationException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }

            try {
                Clock.sleep(this.searchInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private String buildJMXConnectorAddress(VirtualMachineDescriptor newJVM) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        VirtualMachine vm = VirtualMachine.attach(newJVM);

        String connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);


        // no connector address, so we start the JMX agent
        if (connectorAddress == null) {
            String agent = vm.getSystemProperties().getProperty("java.home") +
                    File.separator + "lib" + File.separator +
                    "management-agent.jar";
            vm.loadAgent(agent);

            // agent is started, get the connector address
            connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
        }
        return connectorAddress;
    }
}
