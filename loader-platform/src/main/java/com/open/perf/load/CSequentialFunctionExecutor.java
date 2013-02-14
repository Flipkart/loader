package com.open.perf.load;

import java.util.*;
import java.util.regex.Pattern;

import com.open.perf.common.ClassHelper;
import com.open.perf.operation.FunctionContext;
import com.open.perf.operation.FunctionCounter;
import com.open.perf.operation.FunctionTimer;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Timer;
import org.apache.log4j.Logger;

import com.open.perf.domain.GroupFunctionBean;
import com.open.perf.common.HelperUtil;
import com.open.perf.common.Result;

public class CSequentialFunctionExecutor extends SequentialFunctionExecutor {
    private    long    			       repeat;
    private    long 				   life;
    private    long                    repeatsDone;
    private    long 				   startTime;
    //	private    ArrayList<String> 	   functions;
    private    List<GroupFunctionBean> groupFunctions;
    private    HashMap<String,Object>  params;// Common for all with in the group
    private    boolean 				   forceStop;

    private    boolean                 pause;
    private    static  final int       PAUSE_CHECK_DELAY   =   2000;
    private    boolean                 paused              =   false;  // Even though the pause variable has been set, paused state will be achieved only when current thread cycle is over
    private    boolean                 running             =   true;   // Even though the pause variable has been unset, running state will be achieved only when PAUSE_CHECK_DELAY is over
    private    Map<String,Object>      threadResources;// exclusive resource per thread within

    // This will be used
    //private boolean forceDone = false

    Logger logger      = Logger.getLogger(GroupController.class);
    private static Pattern variablePattern = Pattern.compile(".*(#\\{(.+)\\}).*");
    private Map<String, FunctionTimer> functionTimers;
    private Map<String, FunctionCounter> functionCounters;

    public CSequentialFunctionExecutor(List<GroupFunctionBean> groupFunctions, HashMap<String,Object> params, long repeat, long life,long startTime, ThreadGroup tg, String name) {
        super(tg,name);
        this.repeat       = repeat;
        this.life         = life;
        this.groupFunctions = groupFunctions;
        this.params       = params;
        this.forceStop    = false;
        this.startTime    = startTime;
        this.pause        = false;
        this.functionTimers = new HashMap<String, FunctionTimer>();
        this.functionCounters = new HashMap<String, FunctionCounter>();
    }

    public void run () {
        //this.startTime 	= System.currentTimeMillis();
        FunctionContext functionContext = new FunctionContext(this.functionTimers, this.functionCounters);
        while(repeatAgain() && (this.forceStop == false)) {

		    /*
		     * Add Code for Runtime Pause
		     * Pause would be enabled only when current thread gets over.
		     * Resume will happen only if minimum delay which is 15000 milli seconds have passed
		     */
            if(this.pause) {
                this.paused   =   true;
                this.running  =   false;
                HelperUtil.delay(PAUSE_CHECK_DELAY);
                continue;
            }
            this.running  =   true;
            this.paused   =   false;

            executionOver = false;
            this.resetErroredFunctions();
            this.resetFailedFunctions();

            functionContext.updateParameters(this.params);

            if(threadResources != null) {
//                            threadParam.putAll(this.threadResources);
                functionContext.updateParameters(this.threadResources);
            }

            this.fExecutors = new ArrayList<SyncFunctionExecutor>();

            for(int functionNo = 0; functionNo < this.groupFunctions.size() ; functionNo++) {

                //  We are passing parameters only to the first method and rest of the methods in the sequence
                //  will get them automatically in Sequential Executor.

                GroupFunctionBean groupFunction =   this.groupFunctions.get(functionNo);

                //String function = functions.get(functionNo);
                //int lastIndex 	 = function.lastIndexOf(".");
                String className    = groupFunction.getClassName();
				String functionName = groupFunction.getFunctionName();

                Class functionParamType = FunctionContext.class;
                SyncFunctionExecutor fe;

                try {

                    Object functionClassObject = ClassHelper.getClassInstance(className, groupFunction.getConstructorParamTypes(), groupFunction.getConstructorParams());

                    if(functionNo == 0 ) {
                        functionContext.updateParameters(groupFunction.getParams());
//                        threadParam.putAll(groupFunction.getParams());
//                        resolveVariables(threadParam);
                        fe = new SyncFunctionExecutor(groupFunction.getName(), className, functionName, functionClassObject,new Class[]{functionParamType},new Object[] {functionContext});
                        fe.execute();
                    }
                    else {
                        functionContext.updateParameters(groupFunction.getParams());
                        fe = new SyncFunctionExecutor(groupFunction.getName(), className, functionName, functionClassObject,new Class[]{functionParamType},new Object[] {functionContext});
//                        SyncFunctionExecutor previousFunction = this.fExecutors.get(functionNo-1);
//                        HashMap<String,Object> prevFunctionParams  =   (HashMap<String, Object>) previousFunction.getParams()[0];
//                        prevFunctionParams.putAll(groupFunction.getParams());
//                        resolveVariables(prevFunctionParams);
//                        fe.setParams(new Object[] {prevFunctionParams});
                        fe.execute();
                    }

                    this.fExecutors.add(fe);

                    // If execution Failed because of some Exception/error that occurred while function execution
                    if(fe.isExecutionSuccessful()) {
                        Object returnedObject = fe.getReturnedObject();
                        // If user function is written to return Result Object, then do the check
                        if(returnedObject instanceof Result) {
                            Result result = (Result)returnedObject;
                            // If user functioned decided that function execution was a failure
                            if(result.getResult() == false) {
                                logger.error("Execution of Function " + fe.getAbsoluteFunctionName() + " Failed with reason " + result.getMessage());
                                logger.info("Function '"+fe.getFunctionalityName()+"' failed/errored.");
                                if(functionNo < this.groupFunctions.size()-1)
                                    logger.info("Not running subsequent functions in this repeat.");

                                break;
                            }
                        }
                    }
                    else {
                        logger.error("Execution of Function " + fe.getAbsoluteFunctionName() + " Failed with exception " + fe.getException().getLocalizedMessage());
                        this.exception = fe.getException();
                        this.exceptionCause = fe.getExceptionCause();
                        this.addErroredFunctions(fe); //Am I really using it ?

                        logger.info("Function '"+fe.getFunctionalityName()+"' failed/errored.");
                        if(functionNo < this.groupFunctions.size()-1)
                            logger.info("Not running subsequent functions in this repeat.");

                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    throw new RuntimeException(e);
                }

            }

            endTime = System.currentTimeMillis();
            executionOver = true;

            ((GroupController)this.callBackObject).listener((CSequentialFunctionExecutor)this.callBackParams[0]);
            this.repeatsDone++;
        }
        this.running  =   false;
        logger.info("Function Timers :"+this.functionTimers);
        logger.info("Function Counters :"+this.functionCounters);
        logger.debug("Sequential Function Executor '" + this.getName() + "' Over");
    }

    /**
     * To check if execution have to be repeated again
     * @return
     */
    public boolean repeatAgain() {

        if((this.repeat   <   0)  &&  (this.life  <   0)) {
            return true;
        }

        if(this.life <   0) {
            return this.repeat > this.repeatsDone;
        }

        if(this.repeat < 0)
            return (System.currentTimeMillis() - this.startTime) < this.life;

        else
            return this.repeat > this.repeatsDone;
    }

    public void setLife(long newLife) {
        this.life = newLife;
    }

    public void forceStop() {
        this.forceStop = true;
    }

    public void pause() {
        this.pause =   true;
    }

    public void resume0() {
        this.pause =   false;
    }

    public boolean getPaused() {
        return this.paused;
    }

    public boolean getRunning() {
        return this.running;
    }

/*
    private static void resolveVariables(Map<String,Object> map){
        for(String key : map.keySet()) {
            Object value = map.get(key);
            if(value instanceof String){
                boolean anyChange = false;
                String valueStr = value.toString();
                Matcher m = variablePattern.matcher(valueStr);
                while(m.matches()) {
                    String valueToReplaceWith = null;
                    if (map.containsKey(m.group(2)))
                        valueToReplaceWith = map.get(m.group(2)).toString();

                    valueStr =   valueStr.replace(m.group(1),valueToReplaceWith==null ? "null" : valueToReplaceWith);
                    m = variablePattern.matcher(valueStr);
                    anyChange = true;
                }

                if(anyChange)
                    map.put(key,valueStr);
            }
        }
    }
*/

    public void setThreadResources(Map<String, Object> threadResources) {
        this.threadResources = threadResources;
    }

    public void setFunctionTimers(Map<String,FunctionTimer> functionTimers) {
        this.functionTimers = new HashMap<String, FunctionTimer>();
        for(String functionTimer : functionTimers.keySet()) {
            this.functionTimers.put(functionTimer, new FunctionTimer(functionTimer));
        }
    }

    public void setFunctionCounters(Map<String, FunctionCounter> functionCounters) {
        this.functionCounters = functionCounters;
    }
}
