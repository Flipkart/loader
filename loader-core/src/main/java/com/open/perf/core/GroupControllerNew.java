package com.open.perf.core;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.util.Clock;
import com.open.perf.util.Counter;
import org.apache.log4j.Logger;

import java.io.File;
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
    private long groupStartTime = -1;
    private RequestQueue requestQueue;
    private Map<String, Counter> customCounters;
    private Map<String, FunctionCounter> functionCounters;
    private GroupStatsQueue groupStatsQueue;
    private StatsCollectorThread statsCollectorThread;
    private String basePath;
    private final List<String> ignoreDumpFunctions;

    public GroupControllerNew(String jobId, Group group) {
        this.basePath = System.getProperty("BASE_PATH", "/var/log/loader/");
        basePath += jobId + File.separator + group.getName();

        this.groupName = group.getName();
        this.group = group;
        this.group.getParams().put("GROUP_NAME", this.groupName);
        this.ignoreDumpFunctions = findIgnoredDumpFunctions();

        this.functionCounters = buildFunctionCounters();
        this.customCounters = buildCustomCounter();
        this.requestQueue = buildRequestQueue();
    }

    private List<String> findIgnoredDumpFunctions() {
        List<String> functions = new ArrayList<String>();
        for(GroupFunction gp : this.group.getFunctions())
        if(!gp.isDumpData())
            functions.add(gp.getFunctionalityName());
        return functions;
    }

    /**
     * Building user defined counters
     * @return
     */
    private Map<String, Counter> buildCustomCounter() {
        Map<String, Counter> functionCounters = new HashMap<String, Counter>();
        for(String functionCounterName : group.getCustomCounters())
            functionCounters.put(functionCounterName, new Counter(this.groupName, functionCounterName));
        return functionCounters;
    }

    /**
     * Building counters for user defined functions
     * @return
     */
    private Map<String, FunctionCounter> buildFunctionCounters() {
        Map<String, FunctionCounter> functionCounters = new HashMap<String, FunctionCounter>();
        for(GroupFunction groupFunction :   group.getFunctions()) {
            functionCounters.put(groupFunction.getFunctionalityName(),
                    new FunctionCounter(this.groupName, groupFunction.getFunctionalityName()));
            if(!groupFunction.isDumpData())
                functionCounters.get(groupFunction.getFunctionalityName()).ignore();
        }
        return functionCounters;
    }

    /**
     * Building Request queue that would be shared across all Sequential Function Executors
     * @return
     */
    private RequestQueue buildRequestQueue() {
        RequestQueue requestQueue = null;
        if(group.getRepeats() > 0)
            requestQueue = new RequestQueue(this.groupName, "requestQueue", this.group.getRepeats());
        else
            requestQueue = new RequestQueue(this.groupName, "requestQueue");
        return requestQueue;
    }

    /**
     * Starting the Group Execution
     * @throws InterruptedException
     * @throws FileNotFoundException
     */
    public void start() throws InterruptedException, FileNotFoundException {
        logger.info("************Group Controller "+this.groupName+" Started**************");
        groupStartDelay();

        this.groupStartTime = Clock.nsTick();
        this.groupStatsQueue = new GroupStatsQueue();

        this.sequentialFEs = new ArrayList<SequentialFunctionExecutorNew>();
        this.started = true;
        for(int threadNo=0; threadNo<group.getThreads(); threadNo++) {
            SequentialFunctionExecutorNew sfe = buildSequentialFunctionExecutor(threadNo);

            this.sequentialFEs.add(sfe);
            threadStartDelay();
            sfe.start();
            if(group.getThreadResources().size() > threadNo) {
                sfe.setThreadResources(group.getThreadResources().get(threadNo));
            }
        }

        // Starting stats collection thread. Eventually this might become part of this Group Controller only
        this.statsCollectorThread = new StatsCollectorThread(
                this.basePath,
                this.groupStatsQueue,
                this.functionCounters,
                this.group.getCustomTimers(),
                this.customCounters,
                this.groupStartTime);

        this.statsCollectorThread.start();
    }

    /**
     * induce delay before starting the group
     * @throws InterruptedException
     */
    private void groupStartDelay() throws InterruptedException {
        Thread.sleep(group.getGroupStartDelay());
    }

    /**
     * induce delay before starting a thread
     */
    private void threadStartDelay() {
        Clock.sleep(group.getThreadStartDelay());
    }

    /**
     * Building a sequential Function executor thread. This thread is responsible for executing user functions in sequence in a thread
     * @param threadNo
     * @return
     */
    private SequentialFunctionExecutorNew buildSequentialFunctionExecutor(int threadNo) {
        return new SequentialFunctionExecutorNew(group.getName()+"-"+threadNo,
                this.group.getFunctions(),
                this.group.getParams(),
                this.group.getDuration(),
                this.requestQueue,
                this.groupStartTime,
                this.functionCounters,
                this.customCounters,
                this.group.getCustomTimers(),
                this.groupStatsQueue,
                this.ignoreDumpFunctions,
                this.group.getThroughput() / group.getThreads());
    }

    /**
     * Check all SequentialFunctionExecutor Threads and tell if this group is alive
     * @return
     */
    public boolean isAlive() {
        synchronized (this.sequentialFEs){
            for(SequentialFunctionExecutorNew sfe : this.sequentialFEs)
                if(sfe.isAlive())
                    return true;
        }
        return false;
    }

    /**
     * This function all user to change threads at runtime.
     * @param newThreads
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
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
                    threadStartDelay();
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

    public String getGroupName() {
        return this.groupName;
    }

    public boolean isDead() {
        return !this.isAlive();
    }

    public boolean started() {
        return this.started;
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
        try {
            this.statsCollectorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException(e);
        }
        logger.info("************Group Controller "+this.groupName+" Ended**************");
    }
}
