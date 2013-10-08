package com.flipkart.perf.agent.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/1/13
 * Time: 12:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobStatSyncConfig {
    private int syncInterval;
    private int linesToSyncInOneGo;

    public int getSyncInterval() {
        return syncInterval;
    }

    public JobStatSyncConfig setSyncInterval(int syncInterval) {
        this.syncInterval = syncInterval;
        return this;
    }

    public int getLinesToSyncInOneGo() {
        return linesToSyncInOneGo;
    }

    public JobStatSyncConfig setLinesToSyncInOneGo(int linesToSyncInOneGo) {
        this.linesToSyncInOneGo = linesToSyncInOneGo;
        return this;
    }
}
