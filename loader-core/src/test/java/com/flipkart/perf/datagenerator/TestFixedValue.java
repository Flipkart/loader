package com.flipkart.perf.datagenerator;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/12/13
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestFixedValue {
    @Test
    public void testFixedValue() throws IOException {
        String expectedData = "data";
        DataGenerator fixedDG = new FixedValue(expectedData);
        for(int i=0;i<20;i++) {
            assertThat(fixedDG.next(), is(expectedData));
        }
    }
}
