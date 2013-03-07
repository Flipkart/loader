package com.open.perf.core;

import com.open.perf.domain.GroupFunction;
import com.open.perf.util.ClassHelper;
import com.open.perf.util.Clock;
import com.open.perf.util.Counter;
import com.open.perf.util.Timer;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequentialFunctionExecutorNew extends Thread {
    private static final int PAUSE_CHECK_DELAY   =   200;
    private static Logger logger = Logger.getLogger(SequentialFunctionExecutorNew.class.getName());

    private List<SyncFunctionExecutor> fExecutors;

    private long startTime;
    private long endTime;
    private long duration;
    private int delayInEachIteration;

    private boolean paused = false;
    private boolean running = false;
    private boolean stop = false;
    private boolean over = false;
    private boolean sleeping = false;

    private Map<String,Object> threadResources;
    private List<GroupFunction> groupFunctions;
    private HashMap<String,Object> groupParams;

    private RequestQueue requestQueue;
    private final GroupStatsQueue groupStatsQueue;

    private final Map<String, Counter> customCounters;
    private List<String> customTimerNames;
    private final Map<String, FunctionCounter> functionCounters;
    private final List<String> ignoreDumpFunctions;

    public SequentialFunctionExecutorNew(String threadExecutorName,
                                         List<GroupFunction> groupFunctions,
                                         HashMap<String, Object> groupParams,
                                         long duration,
                                         RequestQueue requestQueue,
                                         long groupStartTime,
                                         Map<String, FunctionCounter> functionCounters,
                                         Map<String, Counter> customCounters,
                                         List<String> customTimerNames,
                                         GroupStatsQueue groupStatsQueue,
                                         List<String> ignoreDumpFunctions) {

        super(threadExecutorName);
        this.ignoreDumpFunctions = ignoreDumpFunctions;
        this.fExecutors = new ArrayList<SyncFunctionExecutor>();
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
        this.setDaemon(true);
    }

    /**
     * Build Function Executor. Building function executors is done only once as an optimization
     * @return
     */
    private List<SyncFunctionExecutor> buildFunctionExecutors() {
        List<SyncFunctionExecutor> fExecutors = new ArrayList<SyncFunctionExecutor>();

        for(GroupFunction groupFunction : this.groupFunctions) {
            try {
                Object functionClassObject = ClassHelper.getClassInstance(
                        groupFunction.getClassName(),
                        new Class[]{},
                        new Object[]{});

                fExecutors.add(
                        new SyncFunctionExecutor(
                                groupFunction.getFunctionalityName(),
                                groupFunction.getClassName(),
                                groupFunction.getFunctionName(),
                                functionClassObject,
                                new Class[]{FunctionContext.class},
                                new Object[] {null}));
            } catch (Exception e) {
                logger.error(e);
                throw new RuntimeException(e);
            }
        }

        return fExecutors;
    }

    public void run () {
        this.startTime 	= System.currentTimeMillis();
        logger.info("Sequential Function Executor "+this.getName()+" started");

        initializeUserFunctions();
        while(canRepeat()) {
            if(this.isPaused()) {
                logger.info(this.getName()+" is paused");
                Clock.sleep(PAUSE_CHECK_DELAY);
                continue;
            }
            this.running  =   true;
            this.sleepInterval();
            this.reset();

            Map<String, Timer> customTimers = buildCustomTimers();
            Map<String, Timer> functionTimers = buildFunctionTimers();

            FunctionContext functionContext = new FunctionContext(customTimers, this.customCounters).
                    updateParameters(this.groupParams).
                    updateParameters(this.threadResources);

            GroupStatsInstance groupStatsInstance = new GroupStatsInstance(customTimers, functionTimers);

            for(int functionNo = 0; functionNo < this.groupFunctions.size(); functionNo++) {
                GroupFunction groupFunction =   this.groupFunctions.get(functionNo);
                String functionalityName = groupFunction.getFunctionalityName();
                FunctionCounter functionCounter = this.functionCounters.get(functionalityName);

                if(functionContext.isSkipFurtherFunctions()) {
                    functionCounter.skipped();
                    continue;
                }
                try {

                    functionContext.updateParameters(groupFunction.getParams());
                    SyncFunctionExecutor fe = this.fExecutors.get(functionNo).
                            setParams(new Object[]{functionContext});

                    fe.execute();

                    functionCounter.executed();
                    if(!this.ignoreDumpFunctions.contains(functionalityName))
                        groupStatsInstance.addFunctionExecutionTime(functionalityName, fe.getExecutionTime());

                    // If execution Failed because of some Exception/error that occurred while function execution
                    if(!fe.isExecutionSuccessful()) {
                        functionCounter.errored();
                        functionContext.skipFurtherFunctions();
                        logger.error("Execution of Function "
                                + fe.getAbsoluteFunctionName()
                                + " stopped with exception ", fe.getException());
                    }
                    else if(functionContext.isCurrentFunctionFailed()) {
                        functionCounter.failed();
                        logger.error("Execution of Function "
                                + fe.getAbsoluteFunctionName()
                                + " Failed with "
                                + functionContext.getFailureType()
                                + " :" +functionContext.getFailureMessage());
                    }

                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    throw new RuntimeException(e);
                }
            }

            groupStatsQueue.addGroupStats(groupStatsInstance);
            sleepInterval();
        }
        destroyUserFunctions();
        this.endTime = System.currentTimeMillis();
        this.running = false;
        this.over = true;

        if(this.duration > (this.endTime - this.startTime)) {
            logger.info("Sequential Function Executor '" + this.getName() + "' Prematurely(" + (this.duration - (this.endTime - this.startTime)) + " ms) Over");
        }
        logger.info("Sequential Function Executor '" + this.getName() + "' Over");
    }


    private void initializeUserFunctions() {
        callFunctionOnUserClass("init");
    }

    private void destroyUserFunctions() {
        callFunctionOnUserClass("end");
    }

    private void callFunctionOnUserClass(String methodName) {
        for(int functionNo = 0; functionNo < this.groupFunctions.size(); functionNo++) {
            GroupFunction groupFunction =   this.groupFunctions.get(functionNo);

            FunctionContext functionContext = new FunctionContext(null, null).
                    updateParameters(this.groupParams).
                    updateParameters(this.threadResources);

            functionContext.updateParameters(groupFunction.getParams());

            SyncFunctionExecutor fe = this.fExecutors.get(functionNo);
            Object classObject = fe.getClassObject();
            try {
                Method m = classObject.getClass().getDeclaredMethod(methodName, new Class[]{FunctionContext.class});
                m.invoke(classObject, new Object[]{functionContext});

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Map<String, Timer> buildFunctionTimers() {
        Map<String, Timer> timersMap = new HashMap<String, Timer>();
        for(GroupFunction gp : this.groupFunctions) {
            timersMap.put(gp.getFunctionalityName(), new Timer("", gp.getFunctionalityName()));
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
     * If there needs to be any throttling, this function would be used.
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

    /**
     * Clean SyncFunctionExecutor Stats
     */
    private void reset() {
        for(SyncFunctionExecutor fe : this.fExecutors)
            fe.reset();
    }

    /**
     * To check if execution have to be repeated again
     * @return
     */
    public boolean canRepeat() {
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