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
public class TestRandomFloat {
    @Test
    public void testRandomNumber() throws IOException {
        float maxValue = 1.0f;
        DataGenerator randomFloat = new RandomFloat();

        for(int i=0;i<10;i++) {
            String data = randomFloat.next();
            assertThat("Data "+data+" should be < " + maxValue, Float.parseFloat(data) < maxValue, is(true));
        }
    }
}
