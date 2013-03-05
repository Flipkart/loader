package com.open.perf.load;

import com.open.perf.domain.GroupFunction;
import com.open.perf.operation.FunctionContext;
import com.open.perf.util.ClassHelper;
import com.open.perf.util.Counter;
import com.open.perf.util.HelperUtil;
import com.open.perf.util.Timer;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequentialFunctionExecutorNew extends Thread {
    private static Logger logger              = Logger.getLogger(SequentialFunctionExecutor.class.getName());
    private List<SyncFunctionExecutor> fExecutors = new ArrayList<SyncFunctionExecutor>();

    private Map<String,Object> threadResources;
    private List<GroupFunction> groupFunctions;
    private HashMap<String,Object> groupParams;

    private List<SyncFunctionExecutor> failedExecutors;
    private List<SyncFunctionExecutor> errorFunctions;

    private long startTime;
    private long endTime;
    private long duration;
    private int delayInEachIteration;

    private static final int PAUSE_CHECK_DELAY   =   200;
    private boolean paused = false;
    private boolean running = false;
    private boolean stop = false;
    private boolean over = false;
    private boolean sleeping = false;

    private Counter requestsDone;
    private RequestQueue requestQueue;
    private final Map<String, Counter> customCounters;
    private final GroupStatsQueue groupStatsQueue;
    private List<String> customTimerNames;
    private final Map<String, FunctionCounter> functionCounters;

    public SequentialFunctionExecutorNew(String threadExecutorName,
                                         List<GroupFunction> groupFunctions,
                                         HashMap<String, Object> groupParams,
                                         long duration,
                                         RequestQueue requestQueue,
                                         long groupStartTime,
                                         Map<String, FunctionCounter> functionCounters,
                                         Map<String, Counter> customCounters,
                                         List<String> customTimerNames,
                                         GroupStatsQueue groupStatsQueue) {

        super(threadExecutorName);
        this.groupStatsQueue = groupStatsQueue;
        this.duration = duration;
        this.groupFunctions = groupFunctions;
        this.groupParams = groupParams;
        this.startTime    = groupStartTime;
        this.requestQueue = requestQueue;
        this.functionCounters = functionCounters;
        this.customCounters = customCounters;
        this.customTimerNames = customTimerNames;
        this.threadResources = new HashMap<String, Object>();


        this.fExecutors = buildFunctionExecutors();
        this.requestsDone = new Counter(threadExecutorName);
        this.errorFunctions = new ArrayList<SyncFunctionExecutor>();
        this.failedExecutors = new ArrayList<SyncFunctionExecutor>();
        this.setDaemon(true);
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
                fExecutors.add(new SyncFunctionExecutor(groupFunction.getName(), className, functionName, functionClassObject,new Class[]{functionParamType},new Object[] {null}));
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
        this.startTime 	= System.currentTimeMillis();
        logger.info("Sequential Function Executor "+this.getName()+" started");
        while(repeat()) {
            // Runtime Pause Facility

            if(this.isPaused()) {
                logger.info(this.getName()+" is paused");
                HelperUtil.delay(PAUSE_CHECK_DELAY);
                continue;
            }
            this.running  =   true;

            // Clear Function Local and Instance variables before next Cycle
            this.reset();

            Map<String, Timer> customTimers = buildCustomTimers();
            Map<String, Timer> functionTimers = buildFunctionTimers();

            FunctionContext functionContext = new FunctionContext(customTimers, this.customCounters);
            functionContext.
                    updateParameters(this.groupParams).
                    updateParameters(this.threadResources);

            GroupStatsInstance groupStatsInstance = new GroupStatsInstance(customTimers, functionTimers);

            for(int functionNo = 0; functionNo < this.groupFunctions.size(); functionNo++) {
                GroupFunction groupFunction =   this.groupFunctions.get(functionNo);
                String uniqueFunctionName = groupFunction.getUniqueFunctionName();
                if(functionContext.isSkipFurtherFunctions()) {
                    logger.info("User asked to skip Further Functions");
                    this.functionCounters.get(uniqueFunctionName).skipped();
                    continue;
                }
                try {

                    functionContext.updateParameters(groupFunction.getParams());
                    SyncFunctionExecutor fe = this.fExecutors.get(functionNo);
                    fe.setParams(new Object[]{functionContext});
                    this.functionCounters.get(uniqueFunctionName).executed();

                    fe.execute();
                    groupStatsInstance.addFunctionExecutionTime(uniqueFunctionName, fe.getExecutionTime());

                    // If execution Failed because of some Exception/error that occurred while function execution
                    if(!fe.isExecutionSuccessful()) {
                        this.functionCounters.get(uniqueFunctionName).errored();

                        logger.error("Execution of Function " + fe.getAbsoluteFunctionName() + " stopped with exception ", fe.getException());
                        errorFunctions.add(fe);

                        logger.info("Function '"+fe.getFunctionalityName()+"' errored.");
                        if(functionNo < this.groupFunctions.size()-1) {
                            logger.info("Not running subsequent functions in this repeat.");
                            for(int skipNo = functionNo+1; skipNo < this.groupFunctions.size(); skipNo++) {
                                this.functionCounters.get(uniqueFunctionName).skipped();
                            }
                        }

                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    throw new RuntimeException(e);
                }
            }

            this.requestsDone.increment();
            groupStatsQueue.addGroupStats(groupStatsInstance);
            sleepInterval();
        }
        this.running = false;
        this.over = false;
        this.endTime = System.currentTimeMillis();
        if(this.duration > (this.endTime - this.startTime)) {
            logger.info("Sequential Function Executor '" + this.getName() + "' Prematurely(" + (this.duration - (this.endTime - this.startTime)) + " ms) Over");
        }
        logger.info("Sequential Function Executor '" + this.getName() + "' Over. Requests Done :"+this.requestsDone.count());
    }

    private Map<String, Timer> buildFunctionTimers() {
        Map<String, Timer> timersMap = new HashMap<String, Timer>();
        for(GroupFunction gp : this.groupFunctions) {
            String uniqueFunctionName = gp.getUniqueFunctionName();
            timersMap.put(uniqueFunctionName, new Timer("", uniqueFunctionName));
        }
        return timersMap;
    }

    private Map<String, Timer> buildCustomTimers() {
        Map<String, Timer> timersMap = new HashMap<String, Timer>();
        for(String timer : this.customTimerNames)
            timersMap.put(timer, new Timer("", timer));
        return timersMap;
    }

    /**
     *
     */
    private void sleepInterval() {
        if(this.delayInEachIteration == 0)
            return;
        logger.info("Going to Sleep for "+this.delayInEachIteration+" ms");
        this.sleeping = true;
        synchronized (this) {
            try {
                this.wait(this.delayInEachIteration);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        this.sleeping = false;
        logger.info("Coming out of sleep");
    }

    private void reset() {
        for(SyncFunctionExecutor fe : this.fExecutors)
            fe.reset();
        this.failedExecutors.clear();
        this.errorFunctions.clear();
    }

    /**
     * To check if execution have to be repeated again
     * @return
     */
    public boolean repeat() {
        if(this.stop)
            return false;

        if(this.duration > 0)
            return (requestQueue.getRequest() && (System.currentTimeMillis() - this.startTime) < this.duration);

        return requestQueue.getRequest();
    }

    public SequentialFunctionExecutorNew setThreadResources(Map<String, Object> threadResources) {
        this.threadResources = threadResources;
        return this;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pauseIt() {
        this.paused = true;
        this.running = false;
    }

    public void resumeIt() {
        this.paused = false;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isStop() {
        return stop;
    }

    public void stopIt() {
        this.stop = true;
        if(this.sleeping) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    public boolean isOver() {
        return over;
    }

    public void setOver(boolean over) {
        this.over = over;
    }

    public int getDelayInEachIteration() {
        return delayInEachIteration;
    }

    public void setDelayInEachIteration(int delayInEachIteration) {
        this.delayInEachIteration = delayInEachIteration;
    }
}
