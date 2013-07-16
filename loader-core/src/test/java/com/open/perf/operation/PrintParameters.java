package com.open.perf.operation;

import com.open.perf.core.FunctionContext;
import com.open.perf.function.PerformanceFunction;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 5/4/13
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class PrintParameters extends PerformanceFunction{
    @Override
    public void execute(FunctionContext context) throws Exception {
        List<String> parameters = context.getParameterAsList("parameters");
        for(String parameter : parameters) {
            logger.info("Parameter '"+parameter+"' : "+context.getParameterAsString(parameter));
        }
    }
}
