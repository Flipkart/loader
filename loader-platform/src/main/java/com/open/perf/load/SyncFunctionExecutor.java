package com.open.perf.load;

import java.lang.reflect.Method;

public class SyncFunctionExecutor {
	 private String name;

	    private Object returnObject ;
	    private Object classObject;
	    private String className ;
	    private String functionName;
	    private Class[] paramTypes;
	    private Object[] params;
	    private boolean success = true;
	    private ClassLoader classLoader;
	    private Throwable exception;
	    private Throwable exceptionCause;
	    private long startTime;
	    private long endTime;
	    public SyncFunctionExecutor(String name, String className, String functionName, Object object, Class[] paramTypes, Object[] params)
	    {
	       this.name = name;
	       this.classObject = object;
	       this.className = className;
	       this.functionName = functionName;
	       this.paramTypes = paramTypes;
	       this.params = params;
	       //   this.setContextClassLoader(Thread.currentThread().getContextClassLoader());
	    }
	    
	    public Object execute() {
	/*
	    	if(classLoader!=null)
	    		Thread.currentThread().setContextClassLoader(classLoader);
	*/
	        returnObject = null;
	        try
	        {
	            Class newClass;
	            if(this.classLoader == null)
	                newClass = Class.forName(this.className);
	            else
	                newClass = Class.forName(this.className,false,this.classLoader);

	            Method method = newClass.getDeclaredMethod(this.functionName,this.paramTypes);
	            startTime = System.nanoTime();
	            returnObject = method.invoke(this.classObject, this.params);
	            endTime = System.nanoTime();
	        }
	        catch(Exception exception)
	        {
	            endTime = System.nanoTime();
	            this.success 		= false;
	            this.exceptionCause = exception.getCause();
	            this.exception 		= exception;
	            if(this.exceptionCause != null)
	                this.exceptionCause.getStackTrace();
	            if(this.exception != null)
	                this.exception.printStackTrace();

	        }
	        return returnObject;
	    }

	    public boolean isExecutionSuccessful()
	    {
	        return this.success;
	    }
	    
	    public Object getReturnedObject()
	    {
	        return this.returnObject;
	    }
	    
	    public Throwable getException()
	    {
	    	return this.exception;
	    }
	    
	    public Throwable getExceptionCause()
	    {
	    	return this.exceptionCause;
	    }

	    //Microseconds
	    public long getExecutionTime() {
	    	return (long)((this.endTime - this.startTime)/1000);
	    }

	    public String getFunctionName() {
	    	return this.functionName;  	
	    }
	    
	    public String getAbsoluteFunctionName() {
	    	return this.className+"."+this.functionName;
	    }
	    
	    public Object[] getParams() {
	    	return this.params;
	    }

	    public void setParams(Object[] params) {
	    	this.params = params;
	    }

	    public String getFunctionalityName() {
	        return name;
	    }

}
