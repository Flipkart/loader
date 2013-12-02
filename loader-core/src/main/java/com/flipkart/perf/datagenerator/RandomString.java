package com.flipkart.perf.datagenerator;

import org.apache.commons.lang.RandomStringUtils;

public class RandomString extends DataGenerator{

    private final int length;
    private final RandomStringType type;
    private final String closedString;

    public static enum RandomStringType{
        NUMERIC, ALPHABETIC, ALPHA_NUMERIC, ANY;
    }

    public RandomString(final RandomStringType type, final int length, String closedString) {
        this.type = type;
        this.length = length;
        this.closedString = closedString;
    }

    @Override
    public String next() {
        if(closedString == null || closedString.trim().equals("")) {
            switch (this.type) {
                case NUMERIC:
                    return RandomStringUtils.randomNumeric(length);
                case ALPHABETIC:
                    return RandomStringUtils.randomAlphabetic(length);
                case ALPHA_NUMERIC:
                    return RandomStringUtils.randomAlphanumeric(length);
                default:
                    return RandomStringUtils.random(length);
            }
        }
        else {
            return RandomStringUtils.random(length, closedString);
        }
    }
}
