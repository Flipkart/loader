package com.open.perf.core;

import com.open.perf.util.Counter;
import com.open.perf.util.Timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private Thread myThread;
    private Map<String, Object> functionParameters;
    private Map<String, Counter> counters;
    private Map<String, Timer> timers;
    private Map<String, Object> passOnParameters; // Will be populated by User in function and would be passed further
    private boolean skipFurtherFunctions = false;
    private FailureType failureType;
    private String failureMessage;
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
     * this will group the parameter based in keys matching the regex, prepare a map and return
     * @param keyMatchingRegEx
     * @return
     */
    public Map<String, Object> getGroupedParameters(String keyMatchingRegEx) {
        Map<String, Object> allParams = new HashMap<String, Object>();
        allParams.putAll(functionParameters);
        allParams.putAll(passOnParameters);

        Map<String,Object> groupedMap = new HashMap<String, Object>();
        for(String key : allParams.keySet()) {
            if(Pattern.matches(keyMatchingRegEx, key))
                groupedMap.put(key, allParams.get(key));
        }
        return groupedMap;
    }

    /**
     * this will group the parameter based in keys matching the regex, prepare a map and return
     * In addition it would reduce the key to the matching group in the regex
     * @param keyMatchingRegEx
     * @param groupId
     * @return
     */
    public Map<String,Object> getGroupedParameters(String keyMatchingRegEx, int groupId) {
        Map<String, Object> allParams = new HashMap<String, Object>();
        allParams.putAll(functionParameters);
        allParams.putAll(passOnParameters);

        Map<String,Object> groupedMap = new HashMap<String, Object>();
        Pattern p = Pattern.compile(keyMatchingRegEx);
        for(String key : allParams.keySet()) {
            Matcher m = p.matcher(key);
            if(m.matches())
                groupedMap.put(m.group(groupId), allParams.get(key));
        }
        return groupedMap;
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
        this.failureMessage = message;
        this.failureType = failureType;
        this.skipFurtherFunctions();
        return this;
    }

    boolean isCurrentFunctionFailed() {
        return currentFunctionFailed;
    }

    public void skipFurtherFunctions() {
        this.skipFurtherFunctions = true;
    }

    boolean isSkipFurtherFunctions() {
        return skipFurtherFunctions;
    }

    public FailureType getFailureType() {
        return failureType;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public FunctionContext setMyThread(Thread myThread) {
        this.myThread = myThread;
        return this;
    }

    public FunctionContext async() {
        try {
            myThread.wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public FunctionContext join() {
        myThread.notify();
        return this;
    }

    public void reset() {
        this.functionParameters.clear();
        this.skipFurtherFunctions = false;
        this.passOnParameters.clear();
        this.currentFunctionFailed = false;
        this.failureMessage=null;
        this.failureType=null;
    }
}
