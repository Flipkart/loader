package com.flipkart.perf.core;

import com.flipkart.perf.common.constant.MathConstant;
import com.flipkart.perf.common.util.*;
import com.flipkart.perf.domain.Group;
import com.flipkart.perf.domain.GroupFunction;
import com.flipkart.perf.util.Counter;
import com.flipkart.perf.util.Histogram;
import com.flipkart.perf.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequentialFunctionExecutor extends Thread {
    private static final int PAUSE_CHECK_DELAY   =   200;
    private static Logger logger = LoggerFactory.getLogger(SequentialFunctionExecutor.class);
    private static final int MILLION = 1000000;
    private static final int MINIMUM_SLEEP_TIME = 10;
    private List<SyncFunctionExecutor> fExecutors;

    private boolean paused = false;
    private boolean running = false;
    private boolean stop = false;
    private boolean over = false;

    private final Group group;
    private Map<String,Object> threadResources;
    private List<GroupFunction> groupFunctions;
    private HashMap<String,Object> groupParams;

    private RequestQueue requestQueue;
    private final RequestQueue warmUpRequestQueue;
    private final GroupStatsQueue groupStatsQueue;

    private final Map<String, Counter> customCounters;
    private final Map<String, FunctionCounter> functionCounters;
    private final List<String> ignoreDumpFunctions;
    private float throughput;
    private long forcedDurationPerIterationNS;
    private long accumulatedSleepIntervalNS; // When This accumulated Sleep Interval Goes above 1 ms then sleep for near by ms value
    private int threadStartDelay;

    public SequentialFunctionExecutor(String threadExecutorName,
                                      Group group,
                                      RequestQueue requestQueue,
                                      RequestQueue warmUpRequestQueue,
                                      Map<String, FunctionCounter> functionCounters,
                                      Map<String, Counter> customCounters,
                                      GroupStatsQueue groupStatsQueue,
                                      List<String> ignoreDumpFunctions) {


        super(threadExecutorName);
        this.group = group;
        this.throughput = group.getThroughput() / group.getThreads();
        this.forcedDurationPerIterationNS = (long)((1000 / this.throughput) * 1000000);

        this.ignoreDumpFunctions = ignoreDumpFunctions;
        this.fExecutors = new ArrayList<SyncFunctionExecutor>();
        this.groupStatsQueue = groupStatsQueue;
        this.groupFunctions = group.getFunctions();
        this.groupParams = group.getParams();
        this.requestQueue = requestQueue;
        this.warmUpRequestQueue = warmUpRequestQueue;
        this.functionCounters = functionCounters;
        this.customCounters = customCounters;
        this.threadResources = new HashMap<String, Object>();
        this.fExecutors = buildFunctionExecutors();
    }

    public int getThreadStartDelay() {
        return threadStartDelay;
    }

    public SequentialFunctionExecutor setThreadStartDelay(int threadStartDelay) {
        this.threadStartDelay = threadStartDelay;
        logger.info("Thread Start Delay :"+this.threadStartDelay);
        return this;
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
                logger.error("Error While building SyncFunctionExecutor", e);
                throw new RuntimeException(e);
            }
        }

        return fExecutors;
    }

    public void run () {
        threadStartDelay();
        initializeUserFunctions();
        doWarmUp();
        doRun();
        destroyUserFunctions();
        this.running = false;
        this.over = true;

    }

    private void doRun() {
        logger.info("Sequential Function Executor '" + this.getName() + "' started");
        Counter repeatCounter = new Counter(this.group.getName(), this.getName(), this.getName());
        while(canRepeat(this.requestQueue)) {
            if(this.isPaused()) {
                logger.info(this.getName()+" is paused");
                try {
                    Clock.sleep(PAUSE_CHECK_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                continue;
            }

            long iterationStartTimeNS = Clock.nsTick();
            this.running  =   true;
            this.reset();

            Map<String, Timer> customTimers = buildCustomTimers();
            Map<String, Timer> functionTimers = buildFunctionTimers();
            Map<String, Histogram> functionHistograms = buildCustomHistograms();

            FunctionContext functionContext = new FunctionContext(customTimers, this.customCounters, functionHistograms).
                    updateParameters(this.groupParams).
                    updateParameters(this.threadResources).
                    setMyThread(this);

            GroupStatsInstance groupStatsInstance = new GroupStatsInstance(customTimers, functionTimers, functionHistograms);

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
                        if(functionContext.getTimeNS() != -1)
                            groupStatsInstance.addFunctionExecutionTime(functionalityName, functionContext.getTimeNS());
                        else
                            groupStatsInstance.addFunctionExecutionTime(functionalityName, fe.getExecutionTimeNS());
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
        logger.info("Sequential Function Executor '" + this.getName() + "' Over. Repeats Done :"+repeatCounter.count());
    }

    private void doWarmUp() {
        logger.info("Sequential Function Executor Warm Up '" + this.getName() + "' started");

        long startTime = Clock.milliTick();
        Counter repeatCounter = new Counter(group.getName(),this.getName(), this.getName());
        while(canRepeat(this.warmUpRequestQueue)) {
            long iterationStartTimeNS = Clock.nsTick();
            this.reset();

            Map<String, Timer> customTimers = buildCustomTimers();
            Map<String, Histogram> functionHistograms = buildCustomHistograms();

            FunctionContext functionContext = new FunctionContext(customTimers, this.customCounters, functionHistograms).
                    updateParameters(this.groupParams).
                    updateParameters(this.threadResources).
                    setMyThread(this);

            for(int functionNo = 0; functionNo < this.groupFunctions.size(); functionNo++) {
                GroupFunction groupFunction =   this.groupFunctions.get(functionNo);

                if(functionContext.isSkipFurtherFunctions()) {
                    logger.warn("Further Functions will be skipped");
                    continue;
                }
                try {

                    functionContext.updateParameters(groupFunction.getParams());
                    SyncFunctionExecutor fe = this.fExecutors.get(functionNo).
                            setParams(new Object[]{functionContext});

                    fe.execute();

                    // If execution Failed because of some Exception/error that occurred while function execution
                    if(!fe.isExecutionSuccessful()) {
                        functionContext.skipFurtherFunctions();
                        logger.error("Execution of Function "
                                + fe.getAbsoluteFunctionName()
                                + " stopped with exception ", fe.getException());
                    }
                    else if(functionContext.isCurrentFunctionFailed()) {
                        logger.error("Execution of Function "
                                + fe.getAbsoluteFunctionName()
                                + " Failed with "
                                + functionContext.getFailureType()
                                + " :" +functionContext.getFailureMessage());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            long iterationTimeNS = Clock.nsTick() - iterationStartTimeNS;
            long iterationSleepIntervalNS   = this.forcedDurationPerIterationNS - iterationTimeNS;
            if(iterationSleepIntervalNS > 0)
                this.accumulatedSleepIntervalNS += iterationSleepIntervalNS;
            repeatCounter.increment();
            sleepInterval();
        }
        logger.info("Sequential Function Executor Warm Up '" + this.getName() + "' Over. Repeats Done :"+repeatCounter.count());
        for(Counter counter : this.customCounters.values())
            counter.reset();
        long warmUpDuration = Clock.milliTick() - startTime;
        if(this.requestQueue.getEndTimeMS() > 0)
            this.requestQueue.setEndTimeMS(this.requestQueue.getEndTimeMS() + warmUpDuration);
    }

    private void threadStartDelay() {
        try {
            logger.info("Sleeping for Thread start Delay :"+threadStartDelay);
            Clock.sleep(this.threadStartDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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

            FunctionContext functionContext = new FunctionContext(null, null, null).
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
            timersMap.put(gp.getFunctionalityName(), new Timer(group.getName(), gp.getFunctionalityName(),gp.getFunctionalityName()));
        }
        return timersMap;
    }

    private Map<String, Timer> buildCustomTimers() {
        Map<String, Timer> timersMap = new HashMap<String, Timer>();
        for(GroupFunction groupFunction : group.getFunctions()) {
            for(String customTimerName : groupFunction.getCustomTimers()) {
                timersMap.put(customTimerName, new Timer(group.getName(), groupFunction.getFunctionalityName(), customTimerName));
            }
        }
        return timersMap;
    }

    private Map<String, Histogram> buildCustomHistograms() {
        Map<String, Histogram> histogramMap = new HashMap<String, Histogram>();
        for(GroupFunction groupFunction : group.getFunctions()) {
            for(String customHistogramName : groupFunction.getCustomHistograms()) {
                histogramMap.put(customHistogramName, new Histogram(group.getName(), groupFunction.getFunctionalityName(), customHistogramName));
            }
        }
        return histogramMap;
    }

    /**
     * If there needs to be any throttling, this function would be used.
     */
    private void sleepInterval() {
        int timeToSleepMS = (int)(this.accumulatedSleepIntervalNS/MILLION);
        if(timeToSleepMS <= MINIMUM_SLEEP_TIME)
            return;

        // Keeping track of missed Nanoseconds
        this.accumulatedSleepIntervalNS = this.forcedDurationPerIterationNS % MILLION;

        logger.debug("Going to Sleep for "+timeToSleepMS +" ms");
        synchronized (this) {
            try {
                Clock.sleep(timeToSleepMS);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
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
    public boolean canRepeat(RequestQueue requestQueue) {
        return !this.stop && requestQueue.hasRequest();
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