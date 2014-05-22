package com.flipkart.perf.datagenerator;


import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 */
public class CyclicSelection extends DataGenerator{
    private final List selectionSet;
    private int index;

    public CyclicSelection(final List selectionSet) {
        index = -1;
        this.selectionSet = selectionSet;
    }

    @Override
    public String next() {
        index++;
        return selectionSet.get(index % selectionSet.size()).toString();
    }
}
