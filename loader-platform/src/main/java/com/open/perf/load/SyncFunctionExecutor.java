package com.open.perf.load;

import java.lang.reflect.Method;

public class SyncFunctionExecutor {
    private String name;

    private Object returnObject ;
    private Object classObject;
    private String className ;
    private String functionName;
    private boolean executionSuccessful = true;
    private Throwable exception;
    private Throwable exceptionCause;
    private long startTime;
    private long endTime;
    private boolean executed = false;
    private final Method method;
    private Object[] params;

    public SyncFunctionExecutor(String name, String className, String functionName, Object object, Class[] functionParamTypes, Object[] params) throws ClassNotFoundException, NoSuchMethodException {
        this.name = name;
        this.classObject = object;
        this.className = className;
        this.functionName = functionName ;
        this.params = params;
        Class newClass = Class.forName(this.className, false, Thread.currentThread().getContextClassLoader());
        this.method = newClass.getDeclaredMethod(this.functionName, functionParamTypes);
    }

    public Object execute() {
        returnObject = null;
        try {
            startTime = System.nanoTime();
            returnObject = method.invoke(this.classObject, params);
            endTime = System.nanoTime();
        }
        catch(Exception exception) {
            endTime = System.nanoTime();
            this.executionSuccessful = false;
            this.exceptionCause = exception.getCause();
            this.exception 		= exception;
            if(this.exceptionCause != null)
                this.exceptionCause.getStackTrace();
            if(this.exception != null)
                this.exception.printStackTrace();

        } finally {
            executed = true;
        }
        return returnObject;
    }

    public boolean isExecutionSuccessful() {
        return this.executionSuccessful;
    }

    public Object getReturnedObject() {
        return this.returnObject;
    }

    public Throwable getException() {
        return this.exception;
    }

    public Throwable getExceptionCause() {
        return this.exceptionCause;
    }

    //Nano Seconds
    public double getExecutionTime() {
        return (this.endTime - this.startTime);
    }

    public String getAbsoluteFunctionName() {
        return this.className+"."+this.functionName;
    }

    public String getFunctionalityName() {
        return name;
    }

    public boolean isExecuted() {
        return executed;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public SyncFunctionExecutor reset() {
        this.returnObject = null;
        this.executionSuccessful = true;
        this.executed =  false;
        return this;
    }
}
