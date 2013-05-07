package server.monitor.collector;

import com.sun.tools.attach.*;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 6/5/13
 * Time: 8:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class DiscoverJVMs {

    private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";
    public static void main(String[] args) throws IOException {

        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (VirtualMachineDescriptor desc : vms) {
            VirtualMachine vm;
            try {
                vm = VirtualMachine.attach(desc);

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
                if(connectorAddress == null)
                    continue;
                JMXServiceURL url = new JMXServiceURL(connectorAddress);
                JMXConnector connector = JMXConnectorFactory.connect(url);
                try {
                    MBeanServerConnection mBeanServerConnection = connector.getMBeanServerConnection();
                    Set<ObjectName> beanSet = mBeanServerConnection.queryNames(null, null);
                    System.out.println(beanSet);
                } finally {
                    connector.close();
                }
            } catch (Exception e) {
            }
        }
    }
}
