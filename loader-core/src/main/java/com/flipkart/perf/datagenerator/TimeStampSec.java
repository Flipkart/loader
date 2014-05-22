package com.flipkart.perf.datagenerator;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 23/10/13
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeStampSec extends DataGenerator{

    @Override
    public String next() {
        return String.valueOf(new Date().getTime()/1000);
    }
}
