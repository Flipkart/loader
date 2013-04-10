package com.open.perf.core;

import com.open.perf.util.Clock;
import com.open.perf.util.Counter;
import com.open.perf.util.Timer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.HashMap;
import java.util.List;
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

    private static ObjectMapper mapper = new ObjectMapper();
    private Thread myThread;
    private Map<String, Object> functionParameters;
    private Map<String, Counter> counters;
    private Map<String, Timer> timers;
    private Map<String, Object> passOnParameters; // Will be populated by User in function and would be passed further
    private boolean skipFurtherFunctions = false;
    private FailureType failureType;
    private String failureMessage;
    private boolean currentFunctionFailed;
    private long startTime = -1;
    private long time = -1;

    public FunctionContext(Map<String,Timer> functionTimers, Map<String,Counter> functionCounters) {
        this.functionParameters = new HashMap<String, Object>();
        this.timers = functionTimers;
        this.counters = functionCounters;
        this.passOnParameters = new HashMap<String, Object>();
    }

    public Object getParameter(String parameterName) {
        Object value = functionParameters.get(parameterName);
        return value != null ? value : passOnParameters.get(parameterName);
    }

    public String getParameterAsString(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : value.toString();
    }

    public Integer getParameterAsInteger(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : Integer.parseInt(getParameter(parameterName).toString());
    }

    public Long getParameterAsLong(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : Long.parseLong(getParameter(parameterName).toString());
    }

    public Float getParameterAsFloat(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : Float.parseFloat(getParameter(parameterName).toString());
    }
    public Double getParameterAsDouble(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : Double.parseDouble(getParameter(parameterName).toString());
    }

    public boolean getParameterAsBoolean(String parameterName) {
        return Boolean.parseBoolean((String)getParameter(parameterName));
    }

    public File getParameterAsFile(String parameterName) {
        Object value = getParameter(parameterName);
        return value == null ? null : new File(getParameter(parameterName).toString());
    }

    public InputStream getParameterAsInputStream(String parameterName) throws FileNotFoundException {
        Object value = getParameter(parameterName);
        return value == null ? null : new FileInputStream(getParameter(parameterName).toString());
    }

    public Map getParameterAsMap(String parameterName) throws IOException {
        Object value = getParameter(parameterName);
        return value == null ? new HashMap() : mapper.readValue(value.toString().replace("'", "\""), Map.class);
    }

    public List getParameterAsList(String parameterName) throws IOException {
        Object value = getParameter(parameterName);
        return value == null ? null : mapper.readValue(value.toString(), List.class);
    }

    /**
     * Get all parameters
     * @return
     */
    public Map<String, Object> getParameters() {
        return getGroupedParameters(".*");
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
        synchronized (myThread) {
            try {
                myThread.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    public FunctionContext join() {
        synchronized (myThread) {
            myThread.notify();
        }
        return this;
    }

    public FunctionContext startMe() {
        this.startTime = Clock.nsTick();
        return this;
    }

    public FunctionContext endMe() {
        if(this.startTime == -1)
            throw new RuntimeException("User calling endMe without calling startMe");
        this.time = Clock.nsTick()- this.startTime;
        return this;
    }

    long getTime() {
        return time;
    }

    public void reset() {
        this.functionParameters.clear();
        this.skipFurtherFunctions = false;
        this.passOnParameters.clear();
        this.currentFunctionFailed = false;
        this.failureMessage=null;
        this.failureType=null;
        this.startTime = -1;
        this.time = -1;
    }
}
