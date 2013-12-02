package com.flipkart.perf.datagenerator;

import java.util.Random;

public class RandomNumber extends DataGenerator{

    private final int maxValue;
    private static final Random random = new Random();
    public RandomNumber(final int maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public String next() {
        return String.valueOf(random.nextInt(maxValue));
    }
}
