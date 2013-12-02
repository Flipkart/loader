package com.flipkart.perf.domain;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean class representing Function Details that you want to run as part of your group
 */
public class GroupFunction implements Cloneable {
	
    private static final String  FUNCTION_NAME = "execute";
	private String functionalityName;
    private String functionClass;
    private boolean dumpData ;
    private Map<String,Object> params ;
    private List<String> customTimers;
    private List<String> customCounters;
    private List<String> customHistograms;

    public GroupFunction() {
    	this.dumpData = false;
    	this.functionClass = null;
    	this.params = new HashMap<String, Object>();
        this.customTimers = new ArrayList<String>();
        this.customCounters = new ArrayList<String>();
        this.customHistograms = new ArrayList<String>();
    }

    public GroupFunction(String functionalityName) {
        this();
        this.functionalityName = functionalityName;
    }

    public static String getFunctionName() {
        return FUNCTION_NAME;
    }

    public String getFunctionClass() {
        return this.functionClass;
    }
    
    public GroupFunction setFunctionClass(String className){
    	this.functionClass = className;
    	return this;
    }

    public GroupFunction setFunctionalityName(String value) {
        this.functionalityName =   value;
        return this;
    }
 
    public String getFunctionalityName() {
        return this.functionalityName;
    }

    public boolean isDumpData() {
        return dumpData;
    }

    public GroupFunction setDumpData(boolean dumpData) {
        this.dumpData = dumpData;
        return this;
    }

    public GroupFunction addParam(String param, Object value) {
        this.params.put(param, value);
        return this;
    }

    public GroupFunction setParams(Map<String, Object> hm){
    	this.params = hm;
    	return this;
    }
    public Map<String,Object> getParams() {
        return this.params;
    }

    public GroupFunction clone() throws CloneNotSupportedException {
        return (GroupFunction) super.clone();
    }

    public List<String> getCustomTimers() {
        return customTimers;
    }

    public GroupFunction setCustomTimers(List<String> customTimers) {
        this.customTimers = customTimers;
        return this;
    }

    public GroupFunction addCustomTimer(String customTimer) {
        this.customTimers.add(customTimer);
        return this;
    }

    public List<String> getCustomCounters() {
        return customCounters;
    }

    public GroupFunction setCustomCounters(List<String> customCounters) {
        this.customCounters = customCounters;
        return this;
    }

    public GroupFunction addCustomCounter(String customCounter) {
        this.customCounters.add(customCounter);
        return this;
    }

    public List<String> getCustomHistograms() {
        return customHistograms;
    }

    public GroupFunction setCustomHistograms(List<String> customHistograms) {
        this.customHistograms = customHistograms;
        return this;
    }

    @JsonIgnore
    public String uniqueFunctionName() {
        return this.functionalityName
                + "." + this.functionClass
                + "." + FUNCTION_NAME;
    }

    public GroupFunction addCustomHistogram(String customHistogram) {
        this.customHistograms.add(customHistogram);
        return this;
    }

    void validate() {
        /**
         * Add Any validation required here
         */
    }

    public String toString() {
        return "Functionality :"+this.functionalityName +", Class :"+this.functionClass;
    }

}
