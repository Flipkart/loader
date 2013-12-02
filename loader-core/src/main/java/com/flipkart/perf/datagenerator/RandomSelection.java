package com.flipkart.perf.datagenerator;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 23/10/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class RandomSelection extends DataGenerator{
    private final String[] selectionSet;
    private static final Random random = new Random();

    public RandomSelection(final String[] selectionSet) {
        this.selectionSet = selectionSet;
    }

    @Override
    public String next() {
        return selectionSet[random.nextInt(selectionSet.length)];
    }
}
