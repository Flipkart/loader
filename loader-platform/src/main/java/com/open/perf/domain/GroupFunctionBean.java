package com.open.perf.domain;

import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class GroupFunctionBean implements Cloneable {
	
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
	private String  functionName ;
	private String  classFunction ;
	private String statFile;
	private String percentileStatFile;
    
    private Object[] constructorParams ;
    private Class[] constructorParamTypes ;
    
    @JsonProperty
    private HashMap<String,Object> params ;
    
    @JsonCreator
    public GroupFunctionBean(@JsonProperty("name")String name) {
    	this.name = name;
    	this.dumpData = false;
    	this.graphIt = false;
    	this.softTimeOut = -1;
    	this.className = null;
    	this.functionName = null;
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

    public GroupFunctionBean setConstructorParams(Object[] constructorParams) {
        this.constructorParams = constructorParams;
        constructorParamTypes = new Class[constructorParams.length];
        for(int i=0 ;i < constructorParams.length;i++) {
            constructorParamTypes[i] = constructorParams[i].getClass();
        }
        return this;
    }
    

    public GroupFunctionBean setFunctionName(String functionName) {
        this.functionName = functionName;
        return this;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public String getClassName() {
        return this.className;
    }
    
    public GroupFunctionBean setClassName(String className){
    	this.className = className;
    	return this;
    }

    public GroupFunctionBean setClassFunction(String value) {
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

    public GroupFunctionBean setSoftTimeOut(int value) {
        this.softTimeOut    = value;
        return this;
    }


    public GroupFunctionBean setName(String value) {
        this.name   =   value;
        return this;
    }
 
    public String getName() {
        return this.name;
    }

    public GroupFunctionBean dumpData() {
        this.dumpData = true;
        return this;
    }

    public GroupFunctionBean doNotDumpData() {
        this.dumpData = false;
        return this;
    }

    public boolean isDumpData() {
        return this.dumpData;
    }

    public GroupFunctionBean graphIt() {
        this.dumpData();
        this.graphIt = true;
        return this;
    }

    public GroupFunctionBean doNotGraphIt() {
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

    public GroupFunctionBean addParam(String param, Object value) {
        this.params.put(param, value);
        return this;
    }

    public GroupFunctionBean setParams(HashMap<String, Object>hm){
    	this.params = hm;
    	return this;
    }
    public HashMap<String,Object> getParams() {
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

    public GroupFunctionBean clone() throws CloneNotSupportedException {
        return (GroupFunctionBean) super.clone();
    }
}
