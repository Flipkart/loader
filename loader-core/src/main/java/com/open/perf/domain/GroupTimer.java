package com.open.perf.domain;

public class GroupTimer {
    private String name;
    private long duration;
    private int threads ;
    private float throughput ;

    public GroupTimer() {
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
}
