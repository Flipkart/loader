package com.open.perf.sample.function;

import com.open.perf.operation.FunctionContext;
import com.open.perf.operation.PerformanceFunction;
import com.open.perf.util.TimerContext;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 14/2/13
 * Time: 10:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class DelayFunction extends PerformanceFunction {

    @Override
    public void init(FunctionContext context) throws Exception {
        logger.info("In init");
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        int delay = context.getParameterAsInteger("delay");
        TimerContext tc = context.getFunctionTimer("timer1").startTimer();
        Thread.sleep(delay);
        tc.stop();
    }

    @Override
    public void end(FunctionContext context) throws Exception {
        logger.info("In end");
    }
}
