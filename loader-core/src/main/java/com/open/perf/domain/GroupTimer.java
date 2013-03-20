package com.open.perf.domain;

import com.open.perf.util.Clock;

public class GroupTimer {
    private String name;
    private long duration;
    private int threads ;
    private float throughput ;
    private long startTimeMS;

    public GroupTimer() {
        this.startTimeMS = Clock.milliTick();
    }

    public String getName() {
        return name;
    }

    public GroupTimer setName(String name) {
        this.name = name;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public GroupTimer setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public int getThreads() {
        return threads;
    }

    public GroupTimer setThreads(int threads) {
        this.threads = threads;
        return this;
    }

    public float getThroughput() {
        return throughput;
    }

    public GroupTimer setThroughput(float throughput) {
        this.throughput = throughput;
        return this;
    }

    public long getStartTimeMS() {
        return startTimeMS;
    }

    /*

    public GroupTimer clone() throws CloneNotSupportedException {
        return (GroupTimer) super.clone();
    }

*/
}
