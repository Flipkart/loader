package com.flipkart.perf.datagenerator;

import java.util.List;

public abstract class DataGenerator {

    abstract public String next();

    public static DataGenerator buildDataGenerator(DataGeneratorInfo info) {
        switch (info.getGeneratorType()) {
            case COUNTER:
                return new Counter(Integer.parseInt(info.getInputDetails().get("startValue").toString()),
                        Integer.parseInt(info.getInputDetails().get("jump").toString()));

            case FIXED_VALUE:
                return new FixedValue(info.getInputDetails().get("startValue").toString());

            case RANDOM_FLOAT:
                return new RandomFloat();

            case RANDOM_NUMBER:
                return new RandomNumber(Integer.parseInt(info.getInputDetails().get("maxValue").toString()));

            case RANDOM_SELECTION:
                return new RandomSelection((String[])info.getInputDetails().get("selectionSet"));

            case RANDOM_STRING:
                return new RandomString(RandomString.RandomStringType.valueOf(info.getInputDetails().get("type").toString()),
                        Integer.parseInt(info.getInputDetails().get("length").toString()),
                        info.getInputDetails().get("closedString").toString());

            case RANDOM_DISTRIBUTION:
                return new RandomDistribution((List<RandomDistribution.DistributionInfo>) info.getInputDetails().get("distributionInfoList"));

            default:
                throw new RuntimeException("Don't know how to create Data Generator of Type "+info.getGeneratorType());
        }

    }
}
