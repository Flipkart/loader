package com.open.perf.common;

import java.io.Serializable;

public class Result implements Serializable {
	private static final long serialVersionUID = 1L;
	String message=null;
    boolean result=false;
    String value = null;
    String state;
    public Result()
    {
        this.message="";
        this.value="";
        this.result=true;
        this.state="";
    }
    public Result(boolean res,String mesg)
    {
        this.result = res;
        this.message = mesg;
    }
    public String getMessage()
    {
        return this.message;
    }
    
    public boolean getResult() 
    {
        return this.result;
    }
    
    public String getValue() 
    {
        return this.value;
    }
    
    public void appendMessage(String mesg)
    {
        this.message = this.message+"\n"+mesg;
    }
    
    public void setMessage(String mesg)
    {
        this.message = mesg;
    }
    
    public void setValue(String val)
    {
        this.value = val;
    }
    
    public void setResult(boolean res)
    {
        this.result = res;
    }
    public void setState(String val)
    {
        this.state = val;
    }
    public String getState()
    {
        return this.state;
    }
    
    public String toString() {
    	return (this.result+" "+this.message+" "+this.value+" "+this.state).trim();
    }
}
