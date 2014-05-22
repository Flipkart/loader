package perf.operation.activemq.util;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nitinka on 24/03/14.
 */
public class JmsConnectionFactoryUtil {
    private static Map<String,ConnectionFactory> jmsConnectionFactories;

    static {
        jmsConnectionFactories = new HashMap<String, ConnectionFactory>();
    }
    synchronized public static ConnectionFactory initialize(String brokerUrl) {
        if(!jmsConnectionFactories.containsKey(brokerUrl)) {
            jmsConnectionFactories.put(brokerUrl, new ActiveMQConnectionFactory(brokerUrl));
        }
        return jmsConnectionFactories.get(brokerUrl);
    }
}
