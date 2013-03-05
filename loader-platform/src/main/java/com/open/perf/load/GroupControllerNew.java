package com.open.perf.load;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.util.Counter;
import com.open.perf.util.HelperUtil;
import com.open.perf.util.Timer;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.*;

public class GroupControllerNew{

    private boolean started = false;

    private String              groupName;
    private static Logger           logger;
    static {
        logger      = Logger.getLogger(GroupControllerNew.class);
    }

    private List<SequentialFunctionExecutorNew> sequentialFEs;
    private final Group group;
    private ArrayList<String> functions;
    private ArrayList<String> ignoreDumpFunctions;
    private long startTime;
    private RequestQueue requestQueue;
    private Map<String, Counter> customCounters;
    private Map<String, FunctionCounter> functionCounters;
    private GroupStatsQueue groupStatsQueue;
    private StatsCollectorThread statsCollectorThread;

    public GroupControllerNew(Group group) throws InterruptedException, FileNotFoundException {
        this.groupName              =   group.getName();

        // No Threads
        if(group.getThreads() < 1) {
            throw new RuntimeException("Group "+this.groupName+": No Threads mentioned for group");
        }
        this.group = group;
        this.group.getParams().put("GROUP_NAME", this.groupName);
        this.sequentialFEs = new ArrayList<SequentialFunctionExecutorNew>();

        this.functions = new ArrayList<String>();
        this.ignoreDumpFunctions = new ArrayList<String>();
        this.functionCounters = new HashMap<String, FunctionCounter>();
        for(GroupFunction groupFunction :   group.getFunctions()) {
            String uniqueFunctionName = groupFunction.getName()+"_"+ groupFunction.getClassName()+"."+groupFunction.getFunctionName();
            this.functions.add(uniqueFunctionName);
            if(!groupFunction.isDumpData())
                this.ignoreDumpFunctions.add(uniqueFunctionName);
            this.functionCounters.put(uniqueFunctionName,
                    new FunctionCounter(this.groupName, uniqueFunctionName));

        }

        this.customCounters = createCustomCounters(group.getCustomCounters());
        this.startTime          =   System.currentTimeMillis();

        if(group.getRepeats() > 0)
            this.requestQueue = new RequestQueue(this.groupName, group.getRepeats() * group.getThreads());
        else
            this.requestQueue = new RequestQueue(this.groupName);

        this.groupStatsQueue = new GroupStatsQueue();
        this.statsCollectorThread = new StatsCollectorThread(this.groupStatsQueue, this.functionCounters, this.group.getCustomTimers(), this.customCounters);
        this.statsCollectorThread.setGroupController(this);
    }

    public void start() throws InterruptedException {
        induceGroupStartDelay();
        this.started = true;
        for(int threadNo=0; threadNo<group.getThreads(); threadNo++) {
            SequentialFunctionExecutorNew sfe = buildSequentialFunctionExecutor(threadNo);

            this.sequentialFEs.add(sfe);
            induceThreadStartDelay();
            sfe.start();
            if(group.getThreadResources().size() > threadNo) {
                sfe.setThreadResources(group.getThreadResources().get(threadNo));
            }
            logger.debug("Group "+this.groupName+" :Thread '"+(threadNo+1)+"' started");
        }
        logger.info("Group "+this.groupName+" :Threads have Started");
        this.statsCollectorThread.start();
    }

    private void induceThreadStartDelay() {
        HelperUtil.delay(group.getThreadStartDelay());
    }

    private void induceGroupStartDelay() throws InterruptedException {
        Thread.sleep(group.getGroupStartDelay());
    }

    private SequentialFunctionExecutorNew buildSequentialFunctionExecutor(int threadNo) {
        return new SequentialFunctionExecutorNew(group.getName()+"-"+threadNo,
                group.getFunctions(),
                group.getParams(),
                group.getDuration(),
                requestQueue,
                startTime,
                this.functionCounters,
                customCounters,
                this.group.getCustomTimers(),
                this.groupStatsQueue);
    }

    public GroupControllerNew setDelayInEachIteration(int delayInEachIteration) {
        synchronized (this.sequentialFEs) {
            for(SequentialFunctionExecutorNew sfe : this.sequentialFEs) {
                sfe.setDelayInEachIteration(delayInEachIteration);
            }
        }
        return this;
    }

    private Map<String, Counter> createCustomCounters(List<String> functionCounterNames) {
        Map<String, Counter> functionCounters = new HashMap<String, Counter>();
        for(String functionCounterName : functionCounterNames)
            functionCounters.put(functionCounterName, new Counter(functionCounterName));
        return functionCounters;
    }

    private Map<String, Timer> createFunctionTimers(List<String> functionTimerNames) {
        Map<String, Timer> timers = new HashMap<String, Timer>();
        for(String functionTimerName : functionTimerNames)
            timers.put(functionTimerName, new Timer(this.groupName, functionTimerName));
        return timers;
    }

    public boolean isAlive() {
        synchronized (this.sequentialFEs){
            for(SequentialFunctionExecutorNew sfe : this.sequentialFEs)
                if(sfe.isAlive())
                    return true;
        }
        return false;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public boolean isDead() {
        return !this.isAlive();
    }

    public boolean started() {
        return this.started;
    }

    public void setThreads(int newThreads) throws NoSuchMethodException, ClassNotFoundException {
        synchronized (this.sequentialFEs)  {
            int currentThreads = this.group.getThreads();
            if(newThreads <= 0) {
                logger.warn("Group " + this.groupName + " :Can't set 0 <= number of threads. Retaining old '" + currentThreads + "' threads.");
            }
            else if(newThreads < currentThreads) {
                int reduceThreads = currentThreads - newThreads;
                logger.debug(reduceThreads+" threads have to be reduced");
                for(int i=1; i<=reduceThreads; i++) {
                    SequentialFunctionExecutorNew sfe = this.sequentialFEs.get(this.sequentialFEs.size()-i);
                    sfe.stopIt();
                    logger.debug("Group "+this.groupName+" : Thread :" + i +" stopped)");
                    this.sequentialFEs.remove(this.sequentialFEs.size()-1);
                    logger.debug("Group " + this.groupName + " :Running Threads :" + this.sequentialFEs.size());
                }

                this.group.setThreads(newThreads);
            }
            else {
                int increasedThreads = newThreads - currentThreads;
                logger.debug("Group "+this.groupName + " :" + increasedThreads+" threads have to be increased");
                for(int i=0; i<increasedThreads; i++) {
                    int threadNo = this.sequentialFEs.size() + 1;
                    SequentialFunctionExecutorNew sfe = buildSequentialFunctionExecutor(threadNo);

                    this.sequentialFEs.add(sfe);
                    induceThreadStartDelay();
                    sfe.start();
                    if(group.getThreadResources().size() > threadNo) {
                        sfe.setThreadResources(group.getThreadResources().get(threadNo));
                    }
                    logger.debug("Group "+this.groupName+" :Thread '"+(threadNo+1)+"' started");
                }

                this.group.setThreads(newThreads);
            }
        }

        logger.info("Total Threads running "+this.sequentialFEs.size()+"(In List) "+this.group.getThreads()+"(ThreadCount)");
    }

    public boolean paused() {
        synchronized (this.sequentialFEs) {
            for(SequentialFunctionExecutorNew sfe : this.sequentialFEs) {
                if(!sfe.isPaused())
                    return false;
            }
        }
        return true;
    }

    public boolean running() {
        synchronized (this.sequentialFEs) {
            for(SequentialFunctionExecutorNew sfe : this.sequentialFEs) {
                if(!sfe.isRunning())
                    return false;
            }
        }
        return true;
    }

    public void stopStatsCollection() {
        this.statsCollectorThread.stopIt();
    }
}
