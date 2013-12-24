package com.flipkart.perf.datagenerator;

import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 23/10/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class RandomSelection extends DataGenerator{
    private final List selectionSet;
    private static final Random random = new Random();

    public RandomSelection(final List selectionList) {
        this.selectionSet = selectionList;
    }

    @Override
    public String next() {
        return selectionSet.get(random.nextInt(selectionSet.size())).toString();
    }
}
