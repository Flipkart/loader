package com.flipkart.perf.core;

import com.flipkart.perf.datagenerator.DataGeneratorInfo;
import com.flipkart.perf.datagenerator.RandomString;
import com.flipkart.perf.domain.Group;
import com.flipkart.perf.domain.GroupFunction;
import com.flipkart.perf.domain.Load;
import com.flipkart.perf.operation.SharedListReaderFunction;
import com.flipkart.perf.operation.SharedListWriterFunction;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 23/10/13
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataGeneratorTest {
    public static void main(String[] args) throws Exception {
        Load load = new Load().
                addDataGenerator(new DataGeneratorInfo().
                        setGeneratorName("myIncrCounter").
                        setGeneratorType(DataGeneratorInfo.DataType.COUNTER).
                        addInput("startValue",1000).
                        addInput("jump",1)).
                addDataGenerator(new DataGeneratorInfo().
                        setGeneratorName("myDecrCounter").
                        setGeneratorType(DataGeneratorInfo.DataType.COUNTER).
                        addInput("startValue", 1000).
                        addInput("jump", -1)).
                addDataGenerator(new DataGeneratorInfo().
                        setGeneratorName("myRandomString").
                        setGeneratorType(DataGeneratorInfo.DataType.RANDOM_STRING).
                        addInput("type", RandomString.RandomStringType.ALPHA_NUMERIC).
                        addInput("length", 10).
                        addInput("closedString", "")).
                addGroup(new Group().
                        setName("QueueProducer").
                        setRepeats(100).
                        addFunction(new GroupFunction().
                                setFunctionClass(SharedListWriterFunction.class.getCanonicalName()).
                                setFunctionalityName("QueueProducer").
                                addParam(SharedListWriterFunction.IP_QUEUE_ELEMENT, "element ${myIncrCounter} ${myDecrCounter} ${myRandomString}")));
        load.start(""+System.currentTimeMillis(), 12345);
    }
}
