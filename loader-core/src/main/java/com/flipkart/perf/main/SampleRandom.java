package com.flipkart.perf.main;

import com.flipkart.perf.common.util.Clock;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.UniformRandomGenerator;

public class SampleRandom {
    public static void main(String[] args) {
        long startTime = Clock.milliTick();
        for(int i=0;i<10;i++)
             System.out.println(RandomStringUtils.random(5, 0,0,false, true,new char[]{'1'}));

        System.out.println("Time Taken :"+(Clock.milliTick() - startTime));
    }
}
