package com.open.perf.domain;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class GroupFunction implements Cloneable {
	
	@JsonProperty
	private String  name ;
	@JsonProperty
    private boolean dumpData ;
	@JsonProperty
	private boolean graphIt ;
	@JsonProperty
	private int     softTimeOut ;
	@JsonProperty
	private String  className ;
	@JsonProperty
	private String  functionName = "execute";
	private String  classFunction ;
	private String statFile;
	private String percentileStatFile;
    
    private Object[] constructorParams ;
    private Class[] constructorParamTypes ;
    
    @JsonProperty
    private Map<String,Object> params ;
    
    @JsonCreator
    public GroupFunction(@JsonProperty("name") String name) {
    	this.name = name;
    	this.dumpData = false;
    	this.graphIt = false;
    	this.softTimeOut = -1;
    	this.className = null;
    	this.classFunction = null;
    	this.params = new HashMap<String, Object>();
    	this.constructorParams = new Object[]{};
    	this.constructorParamTypes = new Class[]{};
    	
    }
    
    public Class[] getConstructorParamTypes() {
        return constructorParamTypes;
    }

    public Object[] getConstructorParams() {
        return constructorParams;
    }

    public GroupFunction setConstructorParams(Object[] constructorParams) {
        this.constructorParams = constructorParams;
        constructorParamTypes = new Class[constructorParams.length];
        for(int i=0 ;i < constructorParams.length;i++) {
            constructorParamTypes[i] = constructorParams[i].getClass();
        }
        return this;
    }
    

    public GroupFunction setFunctionName(String functionName) {
        this.functionName = functionName;
        return this;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public String getClassName() {
        return this.className;
    }
    
    public GroupFunction setClassName(String className){
    	this.className = className;
    	return this;
    }

    public GroupFunction setClassFunction(String value) {
        int lastIndex       =   value.lastIndexOf(".");
        this.className      =   value.substring(0,lastIndex);
        this.functionName   =   value.substring(lastIndex+1);
        this.classFunction  =   value;
        return this;
    }

    public String getClassFunction() {
        return this.className+"."+this.functionName;
    }

    public int getSoftTimeOut() {
        return this.softTimeOut;
    }

    public GroupFunction setSoftTimeOut(int value) {
        this.softTimeOut    = value;
        return this;
    }


    public GroupFunction setName(String value) {
        this.name   =   value;
        return this;
    }
 
    public String getName() {
        return this.name;
    }

    public GroupFunction dumpData() {
        this.dumpData = true;
        return this;
    }

    public GroupFunction doNotDumpData() {
        this.dumpData = false;
        return this;
    }

    public boolean isDumpData() {
        return this.dumpData;
    }

    public GroupFunction graphIt() {
        this.dumpData();
        this.graphIt = true;
        return this;
    }

    public GroupFunction doNotGraphIt() {
        this.doNotDumpData();
        this.graphIt = false;
        return this;
    }

    public boolean isGraphIt() {
        return graphIt;
    }

    public String toString() {
        return "Name :"+this.name+", Function :"+this.className+"."+this.functionName+", Dump Data :"+this.dumpData +", Soft TimeOut :"+this.softTimeOut;
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

    public String getStatFile() {
        return statFile;
    }

    public void setStatFile(String statFile) {
        this.statFile = statFile;
    }

    public void setPercentileStatFile(String percentileStatFile) {
        this.percentileStatFile = percentileStatFile;
    }

    public String getPercentileStatFile() {
        return percentileStatFile;
    }

    public GroupFunction clone() throws CloneNotSupportedException {
        return (GroupFunction) super.clone();
    }
}
