package com.flipkart.perf.core;

import com.flipkart.perf.domain.Group;
import com.flipkart.perf.domain.GroupFunction;
import com.flipkart.perf.domain.Load;
import com.flipkart.perf.operation.SharedListReaderFunction;
import com.flipkart.perf.operation.SharedListWriterFunction;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 26/6/13
 * Time: 10:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class SharedQueueTest {
    public static void main(String[] args) throws Exception {
        Load load = new Load().
                addGroup(new Group().
                        setName("QueueProducer").
                        setRepeats(10000).
                        setThroughput(1000).
                        setThreads(1).
                        addFunction(new GroupFunction().
                                setFunctionClass(SharedListWriterFunction.class.getCanonicalName()).
                                setFunctionalityName("QueueProducer").
                                addParam(SharedListWriterFunction.IP_QUEUE_ELEMENT, "element"))).
                addGroup(new Group().
                        setGroupStartDelay(3000).
                        setName("QueueConsumer").
                        setRepeats(10000).
                        setThroughput(1000).
                        setThreads(1).
                        addFunction(new GroupFunction().
                                setFunctionClass(SharedListReaderFunction.class.getCanonicalName()).
                                setFunctionalityName("QueueConsumer")));

        load.start(""+System.currentTimeMillis(), 12345);
    }
}
