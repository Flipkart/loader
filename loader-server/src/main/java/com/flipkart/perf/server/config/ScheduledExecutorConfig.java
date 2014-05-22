package com.flipkart.perf.server.config;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 7/11/13
 * Time: 8:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScheduledExecutorConfig {
    private int threadPoolSize;
    private int counterCompoundThreadInterval;
    private int counterThroughputThreadInterval;
    private int groupConfConsolidationThreadInterval;
    private int jobDispatcherThreadInterval;
    private int timerComputationThreadInterval;

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public int getCounterCompoundThreadInterval() {
        return counterCompoundThreadInterval;
    }

    public void setCounterCompoundThreadInterval(int counterCompoundThreadInterval) {
        this.counterCompoundThreadInterval = counterCompoundThreadInterval;
    }

    public int getCounterThroughputThreadInterval() {
        return counterThroughputThreadInterval;
    }

    public void setCounterThroughputThreadInterval(int counterThroughputThreadInterval) {
        this.counterThroughputThreadInterval = counterThroughputThreadInterval;
    }

    public int getGroupConfConsolidationThreadInterval() {
        return groupConfConsolidationThreadInterval;
    }

    public void setGroupConfConsolidationThreadInterval(int groupConfConsolidationThreadInterval) {
        this.groupConfConsolidationThreadInterval = groupConfConsolidationThreadInterval;
    }

    public int getJobDispatcherThreadInterval() {
        return jobDispatcherThreadInterval;
    }

    public void setJobDispatcherThreadInterval(int jobDispatcherThreadInterval) {
        this.jobDispatcherThreadInterval = jobDispatcherThreadInterval;
    }

    public int getTimerComputationThreadInterval() {
        return timerComputationThreadInterval;
    }

    public void setTimerComputationThreadInterval(int timerComputationThreadInterval) {
        this.timerComputationThreadInterval = timerComputationThreadInterval;
    }
}
