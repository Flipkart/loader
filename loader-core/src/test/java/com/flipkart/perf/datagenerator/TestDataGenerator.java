package com.flipkart.perf.datagenerator;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/12/13
 * Time: 12:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestDataGenerator {

    @Test
    public void testBuildCounter() throws IOException {
        DataGeneratorInfo info = new DataGeneratorInfo();
        info.setGeneratorName("counter");
        info.setGeneratorType(DataGeneratorInfo.DataType.COUNTER);

        Map<String, Object> generatorInfo = new HashMap<String, Object>();
        generatorInfo.put("startValue",0);
        generatorInfo.put("jump",1);
        generatorInfo.put("maxValue",10);
        info.setInputDetails(generatorInfo);

        DataGenerator counter = DataGenerator.buildDataGenerator(info);
        for(int i=0;i<20;i++) {
            assertThat(counter.next(), is(String.valueOf(i%11)));
        }
    }

    @Test
    public void testBuildRandomNumber() throws IOException {
        DataGeneratorInfo info = new DataGeneratorInfo();
        info.setGeneratorName("randomNumber");
        info.setGeneratorType(DataGeneratorInfo.DataType.RANDOM_NUMBER);

        Map<String, Object> generatorInfo = new HashMap<String, Object>();
        int maxValue = 100;
        generatorInfo.put("maxValue",maxValue);
        info.setInputDetails(generatorInfo);

        DataGenerator randomNumberDG = DataGenerator.buildDataGenerator(info);

        for(int i=0;i<10;i++) {
            String data = randomNumberDG.next();
            assertThat("Data "+data+" should be < " + maxValue, Integer.parseInt(data) < maxValue, is(true));
        }
    }

    @Test
    public void testBuildFixedValue() throws IOException {
        DataGeneratorInfo info = new DataGeneratorInfo();
        info.setGeneratorName("fixedValue");
        info.setGeneratorType(DataGeneratorInfo.DataType.FIXED_VALUE);

        Map<String, Object> generatorInfo = new HashMap<String, Object>();
        String value = "hello";
        generatorInfo.put("value",value);
        info.setInputDetails(generatorInfo);

        DataGenerator fixedDG = DataGenerator.buildDataGenerator(info);

        for(int i=0;i<10;i++) {
            assertThat(fixedDG.next(), is(value));
        }
    }

    @Test
    public void testBuildCyclicSelection() throws IOException {
        DataGeneratorInfo info = new DataGeneratorInfo();
        info.setGeneratorName("cyclicSelection");
        info.setGeneratorType(DataGeneratorInfo.DataType.CYCLIC_SELECTION);

        Map<String, Object> generatorInfo = new HashMap<String, Object>();
        List expectedData = Arrays.asList(new Object[]{"one","two","three"});
        generatorInfo.put("selectionSet", expectedData);
        info.setInputDetails(generatorInfo);

        DataGenerator cyclicDG = DataGenerator.buildDataGenerator(info);
        for(int i=0;i<20;i++) {
            assertThat(cyclicDG.next(), is(expectedData.get(i%expectedData.size())));
        }
    }

    @Test
    public void testUseAndRemove() throws IOException {
        DataGeneratorInfo info = new DataGeneratorInfo();
        info.setGeneratorName("useAndRemove");
        info.setGeneratorType(DataGeneratorInfo.DataType.USE_AND_REMOVE);

        Map<String, Object> generatorInfo = new HashMap<String, Object>();
        LinkedList expectedData = new LinkedList();
        expectedData.add("one");
        expectedData.add("two");
        expectedData.add("three");

        generatorInfo.put("selectionSet", new LinkedList(expectedData));
        info.setInputDetails(generatorInfo);

        DataGenerator useAndRemoveDG = DataGenerator.buildDataGenerator(info);
        int dataSize = expectedData.size();

        for(int i=0;i<dataSize;i++) {
            String selection = useAndRemoveDG.next();
            assertThat("Selection "+selection+" should be present in "+expectedData, expectedData.contains(selection), is(true));
            expectedData.remove(selection);
        }

        for(int i=0;i<5;i++) {
            assertThat(useAndRemoveDG.next(), is(nullValue()));
        }

    }
}
