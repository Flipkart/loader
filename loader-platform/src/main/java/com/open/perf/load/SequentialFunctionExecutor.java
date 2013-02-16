package com.open.perf.load;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.open.perf.operation.FunctionContext;
import org.apache.log4j.Logger;

public class SequentialFunctionExecutor extends Thread{
    private static Logger logger              = Logger.getLogger(SequentialFunctionExecutor.class.getName());
    List<SyncFunctionExecutor> fExecutors;
    List<SyncFunctionExecutor> failedExecutors;
    List<SyncFunctionExecutor> erroredExecutors;

    LinkedHashMap<String,Object> returnedObjects;
    private SyncFunctionExecutor callBackMethodFE;
    private boolean callBackMethodInBG;

    protected boolean executionOver = false;
    protected boolean success = true;
    private ClassLoader classLoader;
    protected Throwable exception;
    protected Throwable exceptionCause;
    protected long startTime;
    protected long endTime;
    protected String callBackClass;
    protected String callBackFunction;
    protected Object callBackObject;
    protected Class[] callBackFunctionParamTypes;
    protected Object[] callBackParams;
    public FunctionContext functionContext;

    public SequentialFunctionExecutor(ArrayList<SyncFunctionExecutor> fes) {
        this.fExecutors = fes;
        this.failedExecutors = new ArrayList<SyncFunctionExecutor>();
        this.erroredExecutors = new ArrayList<SyncFunctionExecutor>();
    }

    public SequentialFunctionExecutor() {
        fExecutors = new ArrayList<SyncFunctionExecutor>();
        this.failedExecutors = new ArrayList<SyncFunctionExecutor>();
        this.erroredExecutors = new ArrayList<SyncFunctionExecutor>();
    }

    public SequentialFunctionExecutor(ThreadGroup tg, String name) {
        super(tg,name);
        fExecutors = new ArrayList<SyncFunctionExecutor>();
        this.failedExecutors = new ArrayList<SyncFunctionExecutor>();
        this.erroredExecutors = new ArrayList<SyncFunctionExecutor>();
    }

    public void addFunctionExecutor(SyncFunctionExecutor fe) {
        if(fExecutors == null)
            fExecutors = new ArrayList<SyncFunctionExecutor>();
        fExecutors.add(fe);
    }

    public void execute() {
        start();
    }

    public void execute(ClassLoader classLoader)
    {
        this.setContextClassLoader(classLoader);
        this.classLoader = classLoader;
        start();
    }

    public boolean isExecutionSuccessful()
    {
        return this.success;
    }

    public boolean isExecutionOver()
    {
        return this.executionOver;
    }

    public LinkedHashMap<String,Object> getReturnedObjects() {
        return this.returnedObjects;
    }

    public List<SyncFunctionExecutor> getFunctionExecutors() {
        return this.fExecutors;
    }

    public Throwable getException()
    {
        return this.exception;
    }

    public Throwable getExceptionCause()
    {
        return this.exceptionCause;
    }

    public long getExecutionTime() {
        return this.endTime - this.startTime;
    }

    public void run() {
        if(classLoader!=null)
            Thread.currentThread().setContextClassLoader(classLoader);
        startTime = System.currentTimeMillis();

        for(int feNo=0 ; feNo < this.fExecutors.size(); feNo++) {

            SyncFunctionExecutor fe = this.fExecutors.get(feNo);
            logger.info("Running '"+fe.getAbsoluteFunctionName()+"'");
            if(feNo==0)
                fe.execute();
            else{
                fe.setParams(this.fExecutors.get(feNo-1).getParams());
                fe.execute();
            }
            if(fe.isExecutionSuccessful() == false) {
                this.exception = fe.getException();
                this.exceptionCause = fe.getExceptionCause();
                break;
            }
        }

        endTime = System.currentTimeMillis();
        executionOver = true;

        if(this.callBackMethodFE != null) {
            this.callBackMethodFE.execute();
        }
    }

    public void setCallBackMethod(String className,String functionName,Object object,Class[] paramTypes,Object[] params, boolean backGround) throws NoSuchMethodException, ClassNotFoundException {
        this.callBackClass = className;
        this.callBackFunction = functionName;
        this.callBackObject = object;
        this.callBackFunctionParamTypes = paramTypes;
        this.callBackParams = params;

        this.callBackMethodFE = new SyncFunctionExecutor("listener", className, functionName, object, paramTypes, params);
        this.callBackMethodInBG = backGround;
    }

    public SyncFunctionExecutor getCallBackMethodExecutor() {
        return this.callBackMethodFE;
    }

    public boolean getCallBackMethodInBG(){
        return this.callBackMethodInBG;
    }

    public List<SyncFunctionExecutor> getFailedFunctions() {
        return this.failedExecutors;
    }

    public List<SyncFunctionExecutor> getErroredFunctions() {
        return this.erroredExecutors;
    }


    public static boolean isStringAndNotEmpty(Object value) {
        if(value.getClass().getName().contains("String")) {
            if(value.toString().length() > 0)
                return true;
        }
        return false;
    }

    public static boolean isIntegerAndNotEmpty(Object value) {
        if(value.getClass().getName().contains("Integer")) {
            try {
                Integer.parseInt(value.toString());
                return true;
            }
            catch(NumberFormatException nfe){
            }
        }
        return false;
    }

    public void resetErroredFunctions() {
        this.erroredExecutors.clear();
    }

    public void resetFailedFunctions() {
        this.failedExecutors.clear();
    }


    public void addErroredFunctions(SyncFunctionExecutor fe) {
        this.erroredExecutors.add(fe);
    }

    public void addSkippedFunction(String skippedFunction) {
        this.addSkippedFunction(skippedFunction);
    }
}
