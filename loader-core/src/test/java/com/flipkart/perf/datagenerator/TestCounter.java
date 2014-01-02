package com.flipkart.perf.datagenerator;

import com.flipkart.perf.core.FunctionContext;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/12/13
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestCounter {
    @Test
    public void testCounterWithMaxValue() throws IOException {
        DataGenerator counter = new Counter(0,1,10);
        for(int i=0;i<20;i++) {
            assertThat(counter.next(), is(String.valueOf(i%11)));
        }
    }

    @Test
    public void testCounterWithJumpAndMAxValue() throws IOException {
        DataGenerator counter = new Counter(0,2,10);
        int[] expectedValues = {0,2,4,6,8,10,0,2,4,6,8,10};
        for(int i=0;i<10;i++) {
            assertThat(counter.next(), is(String.valueOf(expectedValues[i])));
        }
    }
}
