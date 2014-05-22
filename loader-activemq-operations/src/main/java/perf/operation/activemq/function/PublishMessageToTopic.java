package perf.operation.activemq.function;

import com.flipkart.perf.core.FunctionContext;
import com.flipkart.perf.function.FunctionParameter;
import com.flipkart.perf.function.PerformanceFunction;
import perf.operation.activemq.util.JmsConnectionFactoryUtil;

import javax.jms.*;
import java.util.*;

public class PublishMessageToTopic extends PerformanceFunction{
    private Connection connection = null;
    private Session session = null;
    private Map<String, Destination> destinations;
    private Map<String, MessageProducer> producers;

    private static final String IP_BROKER_URL = "brokerUrl";
    private static final String IP_TOPIC_NAME = "topicName";
    private static final String IP_MESSAGE = "message";

    @Override
    public void init(FunctionContext context) throws Exception{
        ConnectionFactory factory = JmsConnectionFactoryUtil.initialize(context.getParameterAsString(IP_BROKER_URL));
        connection = factory.createConnection();
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        connection.start();
        destinations = new HashMap<String, Destination>();
        producers = new HashMap<String, MessageProducer>();
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        String topicName = context.getParameterAsString(IP_TOPIC_NAME);
        Destination destination = destinations.get(topicName);
        if(destination == null) {
            destination = session.createTopic(topicName);
            destinations.put(topicName, destination);
            producers.put(topicName, session.createProducer(destination));
        }
        TextMessage m =session.createTextMessage();
        m.setText(context.getParameterAsString(IP_MESSAGE));
        producers.get(topicName).send(destination, m);
    }

    @Override
    public void end(FunctionContext context) throws Exception{
        for(MessageProducer producer : producers.values()) {
            producer.close();
        }
        session.close();
        connection.close();
    }

    @Override
    public List<String> description() {
        return Arrays.asList(new String[]{"Does Basic Set Function"});
    }

    @Override
    public LinkedHashMap<String, FunctionParameter> inputParameters(){
        LinkedHashMap<String, FunctionParameter> parameters = new LinkedHashMap<String, FunctionParameter>();
        parameters.put(IP_BROKER_URL,
                new FunctionParameter().
                        setName(IP_BROKER_URL).
                        setDefaultValue("tcp://localhost:61616").
                        setDescription("Active MQ Broker Url").
                        setMandatory(true));

        parameters.put(IP_TOPIC_NAME,
                new FunctionParameter().
                        setName(IP_TOPIC_NAME).
                        setDefaultValue(null).
                        setDescription("Topic Name").
                        setMandatory(true));

        parameters.put(IP_MESSAGE,
                new FunctionParameter().
                        setName(IP_MESSAGE).
                        setDefaultValue(null).
                        setDescription("Message To Publish").
                        setMandatory(true));

        return parameters;
    }
}
