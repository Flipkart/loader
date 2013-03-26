package com.open.perf.core;

import com.open.perf.constant.MathConstant;
import com.open.perf.domain.GroupFunction;
import com.open.perf.util.ClassHelper;
import com.open.perf.util.Clock;
import com.open.perf.util.Counter;
import com.open.perf.util.Timer;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequentialFunctionExecutor extends Thread {
    private static final int PAUSE_CHECK_DELAY   =   200;
    private static Logger logger = Logger.getLogger(SequentialFunctionExecutor.class.getName());
    private static final int MILLION = 1000000;
    private static final int MINIMUM_SLEEP_TIME = 10;
    private List<SyncFunctionExecutor> fExecutors;

    private long groupStartTimeNS;
    private long endTime;
    private long durationMS;

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
    private float throughput;
    private int forcedDurationPerIterationNS;
    private int accumulatedSleepIntervalNS; // When This accumulated Sleep Interval Goes above 1 ms then sleep for near by ms value
    private long totalSleepTimeMS = 0;

    public SequentialFunctionExecutor(String threadExecutorName,
                                      List<GroupFunction> groupFunctions,
                                      HashMap<String, Object> groupParams,
                                      long durationMS,
                                      RequestQueue requestQueue,
                                      long groupStartTimeNS,
                                      Map<String, FunctionCounter> functionCounters,
                                      Map<String, Counter> customCounters,
                                      List<String> customTimerNames,
                                      GroupStatsQueue groupStatsQueue,
                                      List<String> ignoreDumpFunctions,
                                      float throughput) {

        super(threadExecutorName);
        this.throughput = throughput;
        this.forcedDurationPerIterationNS = (int)((1000 / this.throughput) * 1000000);

        this.ignoreDumpFunctions = ignoreDumpFunctions;
        this.fExecutors = new ArrayList<SyncFunctionExecutor>();
        this.groupStatsQueue = groupStatsQueue;
        this.durationMS = durationMS;
        this.groupFunctions = groupFunctions;
        this.groupParams = groupParams;
        this.groupStartTimeNS = groupStartTimeNS;
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
                        groupFunction.getFunctionClass(),
                        new Class[]{},
                        new Object[]{});

                fExecutors.add(
                        new SyncFunctionExecutor(
                                groupFunction.getFunctionalityName(),
                                groupFunction.getFunctionClass(),
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
        logger.info("Sequential Function Executor "+this.getName()+" started");
        Counter repeatCounter = new Counter("",this.getName());
        initializeUserFunctions();
        while(canRepeat()) {
            if(this.isPaused()) {
                logger.info(this.getName()+" is paused");
                Clock.sleep(PAUSE_CHECK_DELAY);
                continue;
            }
            long iterationStartTimeNS = Clock.nsTick();
            this.running  =   true;
            this.reset();

            Map<String, Timer> customTimers = buildCustomTimers();
            Map<String, Timer> functionTimers = buildFunctionTimers();

            FunctionContext functionContext = new FunctionContext(customTimers, this.customCounters).
                    updateParameters(this.groupParams).
                    updateParameters(this.threadResources).
                    setMyThread(this);

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
                    if(!this.ignoreDumpFunctions.contains(functionalityName)) {
                        if(functionContext.getTime() != -1)
                            groupStatsInstance.addFunctionExecutionTime(functionalityName, functionContext.getTime());
                        else
                            groupStatsInstance.addFunctionExecutionTime(functionalityName, fe.getExecutionTime());
                    }

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
            long iterationTimeNS = Clock.nsTick() - iterationStartTimeNS;
            long iterationSleepIntervalNS   = this.forcedDurationPerIterationNS - iterationTimeNS;
            if(iterationSleepIntervalNS > 0)
                this.accumulatedSleepIntervalNS += iterationSleepIntervalNS;
            repeatCounter.increment();
            sleepInterval();
        }
        destroyUserFunctions();
        this.endTime = System.currentTimeMillis();
        this.running = false;
        this.over = true;

        if(this.durationMS > (this.endTime - this.groupStartTimeNS)) {
            logger.info("Sequential Function Executor '" + this.getName() + "' Prematurely(" + (this.durationMS - (this.endTime - this.groupStartTimeNS)) + " ms) Over");
        }
        logger.info("Sequential Function Executor '" + this.getName() + "' Over. Repeats Done :"+repeatCounter.count()+". Total Sleep Time: "+this.totalSleepTimeMS+"ms");
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
                Method m = classObject.getClass().getMethod(methodName, new Class[]{FunctionContext.class});
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
        int timeToSleepMS = this.accumulatedSleepIntervalNS/MILLION;
        if(timeToSleepMS <= MINIMUM_SLEEP_TIME)
            return;

        // Keeping track of missed Nanoseconds
        this.accumulatedSleepIntervalNS = this.forcedDurationPerIterationNS % MILLION;

        logger.debug("Going to Sleep for "+timeToSleepMS +" ms");
        this.sleeping = true;
        synchronized (this) {
            try {
                Thread.sleep(timeToSleepMS);
                this.totalSleepTimeMS += timeToSleepMS;
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        this.sleeping = false;
        logger.debug("Coming out of sleep");
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

        if(this.durationMS > 0) {
            return (requestQueue.getRequest() && ((Clock.nsTick() - this.groupStartTimeNS) < this.durationMS * MathConstant.MILLION));

        }

        return requestQueue.getRequest();
    }

    public SequentialFunctionExecutor setThreadResources(Map<String, Object> threadResources) {
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
    }

    public boolean isOver() {
        return over;
    }

    public void setOver(boolean over) {
        this.over = over;
    }

    public void setThroughput(float throughput) {
        this.throughput = throughput;
        this.forcedDurationPerIterationNS = (int)((MathConstant.THOUSAND / this.throughput) * MathConstant.MILLION);
        logger.info(this.getName()+" Expected Throughput :"+this.throughput+ "forcedDurationPerIterationNS: "+this.forcedDurationPerIterationNS);
    }
}