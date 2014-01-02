package com.flipkart.perf.datagenerator;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
public class TestUseAndRemove {
    @Test
    public void testUseAndRemove() throws IOException {
        LinkedList expectedData = new LinkedList();
        expectedData.add("one");
        expectedData.add("two");
        expectedData.add("three");

        DataGenerator useAndRemoveDG = new UseAndRemove(new LinkedList<Object>(expectedData));
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
