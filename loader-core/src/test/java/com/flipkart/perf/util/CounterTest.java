package com.flipkart.perf.util;

import org.testng.annotations.Test;

import java.io.IOException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created with IntelliJ IDEA.
 * User: rahulk
 * Date: 28/07/14
 * Time: 10:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class CounterTest {

    @Test
    public void testIncrementBy() throws Exception {
        Counter counter = new Counter("group", "function", "counter");
        counter.increment(Long.valueOf(5));
        assertThat(counter.count(), is(5L));
    }

    @Test
    public void testIncrement() throws Exception {
        Counter counter = new Counter("group", "function", "counter");
        counter.increment();
        assertThat(counter.count(), is(1L));
    }
}
