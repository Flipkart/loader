package com.open.perf.operation;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.Timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 14/2/13
 * Time: 8:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class FunctionContext {
    private Map<String, Object> functionParameters;
    private Map<String, FunctionCounter> counters;
    private Map<String, FunctionTimer> timers;
    private Map<String, Object> passOnParameters; // Will be populated by User in function and would be passed further

    public FunctionContext(Map<String,FunctionTimer> functionTimers, Map<String,FunctionCounter> functionCounters) {
        this.functionParameters = new HashMap<String, Object>();
        this.timers = functionTimers;
        this.counters = functionCounters;
        this.passOnParameters = new HashMap<String, Object>();
    }

    public String getParameterAsString(String parameterName) {
        return getParameter(parameterName).toString();
    }

    private Object getParameter(String parameterName) {
        Object value = functionParameters.get(parameterName);
        return value != null ? value : passOnParameters.get(parameterName);
    }

    public int getParameterAsInteger(String parameterName) {
        return Integer.parseInt(getParameter(parameterName).toString());
    }

    public long getParameterAsLong(String parameterName) {
        return Long.parseLong(getParameter(parameterName).toString());
    }

    public float getParameterAsFloat(String parameterName) {
        return Float.parseFloat(getParameter(parameterName).toString());
    }
    public double getParameterAsDouble(String parameterName) {
        return Double.parseDouble(getParameter(parameterName).toString());
    }

    public File getParameterAsFile(String parameterName) {
        return new File(getParameter(parameterName).toString());
    }

    public InputStream getParameterAsInputStream(String parameterName) throws FileNotFoundException {
        return new FileInputStream(getParameter(parameterName).toString());
    }

    /**
     * Would Over ride any passed on variable from previous function execution in group
     * @param parameterName
     * @param value
     */
    public void addParameter(String parameterName, Object value) {
        passOnParameters.put(parameterName, value);
    }

    public FunctionTimer getFunctionTimer(String timerName) {
        FunctionTimer functionTimer = this.timers.get(timerName);
        if(functionTimer == null)
            throw new RuntimeException("Timer "+timerName+" doesn't exist");
        return functionTimer;
    }

    public FunctionCounter getFunctionCounter(String counterName) {
        FunctionCounter functionCounter = this.counters.get(counterName);
        if(functionCounter == null)
            throw new RuntimeException("Counter "+counterName+" doesn't exist");
        return functionCounter;
    }

    public void updateParameters(Map<String, Object> params) {
        this.functionParameters.putAll(params);
    }
}
