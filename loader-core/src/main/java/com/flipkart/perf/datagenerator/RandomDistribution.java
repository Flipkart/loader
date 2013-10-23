package com.flipkart.perf.datagenerator;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 23/10/13
 * Time: 3:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class RandomDistribution extends DataGenerator{
    private int maxValue = -1;
    private static Random random = new Random();

    public static class DistributionInfo {
        private int start, end;
        private String value;

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean include(int value) {
            return value >= start && value <= end;
        }
    }
    private List<DistributionInfo> distributionInfoMap;

    public RandomDistribution(List<DistributionInfo> distributionInfoList) {
        this.distributionInfoMap = distributionInfoList;
        for(DistributionInfo di : distributionInfoList) {
            if(di.end > maxValue)
                maxValue = di.end;
        }
    }

    @Override
    public String next() {
        for(DistributionInfo di : distributionInfoMap)
            if(di.include(random.nextInt(maxValue) + 1))
                return di.getValue();
        return null;
    }
}
