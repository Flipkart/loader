package com.open.perf.sample.function;

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
public class DummyFunction extends PerformanceFunction {

    @Override
    public void init(FunctionContext context) throws Exception {
        logger.info("In init");
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
    }

    @Override
    public void end(FunctionContext context) throws Exception {
        logger.info("In end");
    }
}
