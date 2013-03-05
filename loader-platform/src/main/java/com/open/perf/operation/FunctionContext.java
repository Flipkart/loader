package com.open.perf.operation;

import com.open.perf.util.Counter;
import com.open.perf.util.Timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 14/2/13
 * Time: 8:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class FunctionContext {
    public static enum FailureType {
        GENERAL, DATA_MISSING, FUNCTIONAL_FAILURE, WRONG_INPUT
    }

    private Map<String, Object> functionParameters;
    private Map<String, Counter> counters;
    private Map<String, Timer> timers;
    private Map<String, Object> passOnParameters; // Will be populated by User in function and would be passed further
    private boolean skipFurtherFunctions = false;
    private boolean currentFunctionFailed;

    public FunctionContext(Map<String,Timer> functionTimers, Map<String,Counter> functionCounters) {
        this.functionParameters = new HashMap<String, Object>();
        this.timers = functionTimers;
        this.counters = functionCounters;
        this.passOnParameters = new HashMap<String, Object>();
    }

    private Object getParameter(String parameterName) {
        Object value = functionParameters.get(parameterName);
        return value != null ? value : passOnParameters.get(parameterName);
    }

    public String getParameterAsString(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : value.toString();
    }

    public int getParameterAsInteger(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : Integer.parseInt(getParameter(parameterName).toString());
    }

    public long getParameterAsLong(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : Long.parseLong(getParameter(parameterName).toString());
    }

    public float getParameterAsFloat(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : Float.parseFloat(getParameter(parameterName).toString());
    }
    public double getParameterAsDouble(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : Double.parseDouble(getParameter(parameterName).toString());
    }

    public File getParameterAsFile(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : new File(getParameter(parameterName).toString());
    }

    public InputStream getParameterAsInputStream(String parameterName) throws FileNotFoundException {
        Object value = getParameter(parameterName);
        return value == null ? null : new FileInputStream(getParameter(parameterName).toString());
    }

    /**
     * Would Over ride any passed on variable from previous function execution in group
     * @param parameterName
     * @param value
     */
    public FunctionContext addParameter(String parameterName, Object value) {
        passOnParameters.put(parameterName, value);
        return this;
    }

    public Timer getFunctionTimer(String timerName) {
        return this.timers.get(timerName);
    }

    public Counter getFunctionCounter(String counterName) {
        Counter counter = this.counters.get(counterName);
        if(counter == null)
            throw new RuntimeException("Counter "+counterName+" doesn't exist");
        return counter;
    }

    public FunctionContext updateParameters(Map<String, Object> params) {
        this.functionParameters.putAll(params);
        return this;
    }

    public FunctionContext failed(FailureType failureType, String message) {
        this.currentFunctionFailed = true;
        this.skipFurtherFunctions();
        return this;
    }

    public boolean isCurrentFunctionFailed() {
        return currentFunctionFailed;
    }

    public void setCurrentFunctionFailed(boolean currentFunctionFailed) {
        this.currentFunctionFailed = currentFunctionFailed;
    }

    public void skipFurtherFunctions() {
        this.skipFurtherFunctions = true;
    }

    public boolean isSkipFurtherFunctions() {
        return skipFurtherFunctions;
    }

    public void reset() {
        this.functionParameters.clear();
        this.skipFurtherFunctions = false;
        this.passOnParameters.clear();
        this.currentFunctionFailed = false;
    }
}
