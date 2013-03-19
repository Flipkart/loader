package com.open.perf.sample.function;

import com.open.perf.core.FunctionContext;
import com.open.perf.function.FunctionParameter;
import com.open.perf.function.PerformanceFunction;
import com.open.perf.util.TimerContext;

import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 14/2/13
 * Time: 10:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class DelayFunction extends PerformanceFunction {
    public static final String DELAY = "DELAY";

    @Override
    public void init(FunctionContext context) throws Exception {
        logger.info("In init");
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        int delay = context.getParameterAsInteger(DELAY);
        TimerContext timer = context.getFunctionTimer("timer1").startTimer();
        Thread.sleep(delay);
        timer.stop();
    }

    @Override
    public void end(FunctionContext context) throws Exception {
        logger.info("In end");
    }

    @Override
    public LinkedHashMap<String, FunctionParameter> inputParameters() {
        LinkedHashMap<String, FunctionParameter> parameters = new LinkedHashMap<String, FunctionParameter>();
        parameters.put(DELAY, new FunctionParameter().
                setName(DELAY).
                setDefaultValue(1).
                setMandatory(true).
                setDescription("Introduces Delay in function call"));
        return parameters;
    }

    @Override
    public LinkedHashMap<String, FunctionParameter> outputParameters() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
