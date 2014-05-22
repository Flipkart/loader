package com.flipkart.perf.datagenerator;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/12/13
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestCyclicSelection {
    @Test
    public void testCyclicSelection() throws IOException {
        Object[] expectedData = new Object[]{"one","two","three"};
        DataGenerator cyclicDG = new CyclicSelection(Arrays.asList(expectedData));
        for(int i=0;i<20;i++) {
            assertThat(cyclicDG.next(), is(expectedData[i%expectedData.length]));
        }
    }
}
