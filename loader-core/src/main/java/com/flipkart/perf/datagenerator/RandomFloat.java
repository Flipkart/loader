package com.flipkart.perf.datagenerator;

import java.util.Random;

public class RandomFloat extends DataGenerator{

    private static final Random random = new Random();

    @Override
    public String next() {
        return String.valueOf(random.nextFloat());
    }
}
