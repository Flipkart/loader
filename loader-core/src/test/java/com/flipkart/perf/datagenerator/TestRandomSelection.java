package com.flipkart.perf.datagenerator;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/12/13
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestRandomSelection {
    @Test
    public void testRandomSelection() throws IOException {
        List expectedData = new ArrayList();
        expectedData.add("one");
        expectedData.add("two");

        DataGenerator randomSelectionDG = new RandomSelection(expectedData);
        for(int i=0;i<20;i++) {
            String selection = randomSelectionDG.next();
            assertThat("Selection "+selection+" should be present in "+expectedData, expectedData.contains(selection), is(true));
        }
    }
}
