package com.open.perf.sample.function;

import com.open.perf.core.FunctionContext;
import com.open.perf.function.PerformanceFunction;
import com.open.perf.util.TimerContext;

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
        TimerContext timer = context.getFunctionTimer("timer1").startTimer();
        Thread.sleep(delay);
        timer.stop();
    }

    @Override
    public void end(FunctionContext context) throws Exception {
        logger.info("In end");
    }
}
