package com.open.perf.sample.function;

import com.open.perf.util.Counter;
import com.open.perf.core.FunctionContext;
import com.open.perf.function.PerformanceFunction;

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
        logger.info("In execute");
    }

    @Override
    public void end(FunctionContext context) throws Exception {
        logger.info("In end");
    }
}
