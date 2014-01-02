package com.flipkart.perf.datagenerator;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/12/13
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestRandomNumber {
    @Test
    public void testRandomNumber() throws IOException {
        int maxValue = 100;
        DataGenerator randomNumberDG = new RandomNumber(maxValue);

        for(int i=0;i<10;i++) {
            String data = randomNumberDG.next();
            assertThat("Data "+data+" should be < " + maxValue, Integer.parseInt(data) < maxValue, is(true));
        }
    }
}
