package com.open.perf.core;

public class CSequentialFunctionExecutor extends SequentialFunctionExecutor {
/*
    private    long    			       repeat;
    private    long 				   life;
    private    long                    repeatsDone;
    private    long 				   startTime;
    //	private    ArrayList<String> 	   functions;
    private    List<GroupFunction> groupFunctions;
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
    private Map<String, Timer> functionTimers;
    private Map<String, Counter> functionCounters;

    public CSequentialFunctionExecutor(List<GroupFunction> groupFunctions, HashMap<String,Object> params, long repeat, long life,long startTime, ThreadGroup tg, String name) {
        super(tg,name);
        this.repeat       = repeat;
        this.life         = life;
        this.groupFunctions = groupFunctions;
        this.params       = params;
        this.forceStop    = false;
        this.startTime    = startTime;
        this.pause        = false;
        this.functionTimers = new HashMap<String, Timer>();
        this.functionCounters = new HashMap<String, Counter>();

    }

    private List<SyncFunctionExecutor> buildFunctionExecutors() {
        List<SyncFunctionExecutor> fExecutors = new ArrayList<SyncFunctionExecutor>();

        for(GroupFunction groupFunction : this.groupFunctions) {
            String className    = groupFunction.getClassName();
            String functionName = groupFunction.getFunctionName();

            Class functionParamType = FunctionContext.class;
            Object functionClassObject = null;
            try {
                functionClassObject = ClassHelper.getClassInstance(className, groupFunction.getConstructorParamTypes(), groupFunction.getConstructorParams());
                fExecutors.add(new SyncFunctionExecutor(groupFunction.getName(), className, functionName, functionClassObject,new Class[]{functionParamType},new Object[] {functionContext}));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            }
        }

        return fExecutors;
    }

    public void run () {
        //this.startTime 	= System.currentTimeMillis();
        this.functionContext = new FunctionContext(this.functionTimers, this.functionCounters);
        this.fExecutors = buildFunctionExecutors();

        while(repeatAgain() && (this.forceStop == false)) {
            functionContext.reset();
		    */
/*
		     * Add Code for Runtime Pause
		     * Pause would be enabled only when current thread gets over.
		     * Resume will happen only if minimum delay which is 15000 milli seconds have passed
		     *//*

            if(this.pause) {
                this.paused   =   true;
                this.running  =   false;
                HelperUtil.delay(PAUSE_CHECK_DELAY);
                continue;
            }
            this.running  =   true;
            this.paused   =   false;

            executionOver = false;

            reset();

            functionContext.updateParameters(this.params);

            if(threadResources != null) {
                functionContext.updateParameters(this.threadResources);
            }


            for(int functionNo = 0; functionNo < this.groupFunctions.size(); functionNo++) {
                if(functionContext.isSkipFurtherFunctions()) {
                    logger.info("Skipping Further Functions");
                    break;
                }
                GroupFunction groupFunction =   this.groupFunctions.get(functionNo);

                try {

                    functionContext.updateParameters(groupFunction.getParams());
                    SyncFunctionExecutor fe = this.fExecutors.get(functionNo);
                    fe.execute();

                    // If execution Failed because of some Exception/error that occurred while function execution
                    if(!fe.isExecutionSuccessful()) {
                        logger.error("Execution of Function " + fe.getAbsoluteFunctionName() + " Failed", fe.getException());
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

            try {
                ((GroupController)this.callBackObject).listener((CSequentialFunctionExecutor)this.callBackParams[0]);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            }
            this.repeatsDone++;
        }
        this.running  =   false;
        logger.debug("Sequential Function Executor '" + this.getName() + "' Over");
    }

    private void reset() {
        this.resetErroredFunctions();
        this.resetFailedFunctions();
        for(SyncFunctionExecutor fe : this.fExecutors)
            fe.reset();
    }

    */
/**
     * To check if execution have to be repeated again
     * @return
     *//*

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

*/
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
*//*


    public void setThreadResources(Map<String, Object> threadResources) {
        this.threadResources = threadResources;
    }

    public void setFunctionTimers(Map<String,Timer> functionTimers) {
        this.functionTimers = new HashMap<String, Timer>();
        for(String functionTimer : functionTimers.keySet()) {
            this.functionTimers.put(functionTimer, new Timer("CHANGE THIS tO GROUP NAME",functionTimer));
        }
    }

    public void setFunctionCounters(Map<String, Counter> functionCounters) {
        this.functionCounters = functionCounters;
    }
*/
}
