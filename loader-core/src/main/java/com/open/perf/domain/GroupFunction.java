package com.open.perf.domain;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.HashMap;
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
    
    public GroupFunction() {
    	this.dumpData = false;
    	this.functionClass = null;
    	this.params = new HashMap<String, Object>();
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

    public GroupFunction dumpData() {
        this.dumpData = true;
        return this;
    }

    public boolean isDumpData() {
        return this.dumpData;
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

    @JsonIgnore
    public String uniqueFunctionName() {
        return this.functionalityName
                + "." + this.functionClass
                + "." + FUNCTION_NAME;
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
