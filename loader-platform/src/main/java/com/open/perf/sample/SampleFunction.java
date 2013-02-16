package com.open.perf.sample;

import com.open.perf.util.Counter;
import com.open.perf.operation.FunctionContext;
import com.open.perf.operation.PerformanceFunction;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 14/2/13
 * Time: 10:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class SampleFunction extends PerformanceFunction {
    @Override
    public void init(FunctionContext context) throws Exception {
        logger.info("In init");
    }

    @Override
    public void execute(FunctionContext context) throws Exception {

        context.getFunctionTimer("sleeper").startTimer();
        Thread.sleep(new Random().nextInt(100));
        context.getFunctionTimer("sleeper").endTimer();

        Counter testCounter = context.getFunctionCounter("testCounter");
        testCounter.increment();
        logger.info("Params1 :"+context.getParameterAsString("paramF1"));
        logger.info("Params2 :"+context.getParameterAsString("paramF2"));

        if(testCounter.count() % 100 == 0)
            throw new RuntimeException("Just Like That Exception");
//        context.skipFurtherFunctions();
    }

    @Override
    public void end(FunctionContext context) throws Exception {
        logger.info("In end");
    }
}
