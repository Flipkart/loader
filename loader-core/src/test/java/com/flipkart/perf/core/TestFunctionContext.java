package com.flipkart.perf.core;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.*;

import static org.hamcrest.MatcherAssert.*;

import com.flipkart.perf.datagenerator.Counter;
import com.flipkart.perf.datagenerator.DataGenerator;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import sun.applet.resources.MsgAppletViewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 20/12/13
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestFunctionContext {

    @Test
    public void testGetVariableAsString() throws IOException {
        FunctionContext functionContext = new FunctionContext(null, null, null, null);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("Param1", "Value1");
        functionContext.updateParameters(parameters);
        assertThat(functionContext.getParameterAsString("Param1"), is(parameters.get("Param1")));
    }

    @Test
    public void testGetVariableAsInteger() throws IOException {
        FunctionContext functionContext = new FunctionContext(null, null, null, null);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("Param1", 10);
        functionContext.updateParameters(parameters);
        assertThat(functionContext.getParameterAsInteger("Param1"), is(parameters.get("Param1")));
    }

    @Test
    public void testGetVariableAsFloat() throws IOException {
        FunctionContext functionContext = new FunctionContext(null, null, null, null);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("Param1", 10.0f);
        functionContext.updateParameters(parameters);
        assertThat(functionContext.getParameterAsFloat("Param1"), is(parameters.get("Param1")));
    }

    @Test
    public void testGetVariableAsBoolean() throws IOException {
        FunctionContext functionContext = new FunctionContext(null, null, null, null);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("Param1", true);
        functionContext.updateParameters(parameters);
        assertThat(functionContext.getParameterAsBoolean("Param1"), is(parameters.get("Param1")));
    }

    @Test
    public void testGetVariableAsBooleanString() throws IOException {
        FunctionContext functionContext = new FunctionContext(null, null, null, null);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("Param1", "true");
        functionContext.updateParameters(parameters);
        assertThat(functionContext.getParameterAsBoolean("Param1"), is(true));
    }

    @Test
    public void testGetVariableAsMap() throws IOException {
        FunctionContext functionContext = new FunctionContext(null, null, null, null);
        Map<String, Object> parameters = new HashMap<String, Object>();
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put("K1","V1");
        parameters.put("Param1", valueMap);
        functionContext.updateParameters(parameters);
        assertThat(functionContext.getParameterAsMap("Param1"), is(parameters.get("Param1")));
    }

    @Test
    public void testGetVariableAsList() throws IOException {
        FunctionContext functionContext = new FunctionContext(null, null, null, null);
        Map<String, Object> parameters = new HashMap<String, Object>();
        List<String> valueList = new ArrayList<String>();
        valueList.add("V1");
        parameters.put("Param1", valueList);
        functionContext.updateParameters(parameters);
        assertThat(functionContext.getParameterAsList("Param1"), is(parameters.get("Param1")));
    }

    @Test
    public void testGetVariableAsStringWithGroupCounterGenerator() throws IOException {
        DataGenerator generator = new Counter(0,1,100);
        Map<String, DataGenerator> dataGenerators = new HashMap<String, DataGenerator>();
        dataGenerators.put("Counter", generator);

        FunctionContext functionContext = new FunctionContext(null, null, null, dataGenerators);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("Param1", "Value1 : ${Counter}");
        functionContext.updateParameters(parameters);

        for(int i=0;i<10;i++)
            assertThat(functionContext.getParameterAsString("Param1"), is("Value1 : "+i));
    }


    @Test
    public void testGetVariableAsMapWithGroupCounterGenerator() throws IOException {
        DataGenerator generator = new Counter(0,1,100);
        Map<String, DataGenerator> dataGenerators = new HashMap<String, DataGenerator>();
        dataGenerators.put("Counter", generator);

        FunctionContext functionContext = new FunctionContext(null, null, null, dataGenerators);
        Map<String, Object> parameters = new HashMap<String, Object>();
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put("K1","Value1 : ${Counter}");
        parameters.put("Param1", valueMap);
        functionContext.updateParameters(parameters);
        for(int i=0;i<10;i++)
            assertThat(functionContext.getParameterAsMap("Param1").get("K1").toString(), is("Value1 : "+i));
    }

    @Test
    public void testGetVariableAsListWithGroupCounterGenerator() throws IOException {
        DataGenerator generator = new Counter(0,1,100);
        Map<String, DataGenerator> dataGenerators = new HashMap<String, DataGenerator>();
        dataGenerators.put("Counter", generator);

        FunctionContext functionContext = new FunctionContext(null, null, null, dataGenerators);
        Map<String, Object> parameters = new HashMap<String, Object>();
        List<String> valueList = new ArrayList<String>();
        valueList.add("Value1 : ${Counter}");
        parameters.put("Param1", valueList);
        functionContext.updateParameters(parameters);
        for(int i=0;i<10;i++)
            assertThat(functionContext.getParameterAsList("Param1").get(0).toString(), is("Value1 : "+i));
    }

    @Test
    public void testGetVariableAsIntegerWithGroupCounter() throws IOException {
        DataGenerator generator = new Counter(0,1,100);
        Map<String, DataGenerator> dataGenerators = new HashMap<String, DataGenerator>();
        dataGenerators.put("Counter", generator);

        FunctionContext functionContext = new FunctionContext(null, null, null, dataGenerators);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("Param1", "${Counter}");
        functionContext.updateParameters(parameters);

        for(int i=0;i<10;i++)
            assertThat(functionContext.getParameterAsInteger("Param1"), is(i));
    }

}
