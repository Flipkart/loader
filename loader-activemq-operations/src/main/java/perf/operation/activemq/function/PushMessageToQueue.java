package perf.operation.activemq.function;

import com.flipkart.perf.core.FunctionContext;
import com.flipkart.perf.function.FunctionParameter;
import com.flipkart.perf.function.PerformanceFunction;
import perf.operation.activemq.util.JmsConnectionFactoryUtil;

import javax.jms.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class PushMessageToQueue extends PerformanceFunction{
    private Connection connection = null;
    private Session session = null;
    private Destination destination;
    private MessageProducer producer;

    private static final String IP_BROKER_URL = "brokerUrl";
    private static final String IP_QUEUE_NAME = "queueName";
    private static final String IP_MESSAGE = "message";

    @Override
    public void init(FunctionContext context) throws Exception{
        ConnectionFactory factory = JmsConnectionFactoryUtil.initialize(context.getParameterAsString(IP_BROKER_URL));
        connection = factory.createConnection();
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        connection.start();
        destination = session.createQueue(context.getParameterAsString(IP_QUEUE_NAME));
        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        TextMessage m =session.createTextMessage();
        m.setText(context.getParameterAsString(IP_MESSAGE));
        producer.send(destination, m);
    }

    @Override
    public void end(FunctionContext context) throws Exception{
        producer.close();
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

        parameters.put(IP_QUEUE_NAME,
                new FunctionParameter().
                        setName(IP_QUEUE_NAME).
                        setDefaultValue(null).
                        setDescription("Queue Name").
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
