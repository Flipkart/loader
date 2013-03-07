package com.open.perf.domain;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class GroupTimer {
    private String name;
    private String delayAfterRepeats;
    private long runtime;
    private long repeats ;
    private int threads ;
    private long startTime;
    private long repeatsDone;

    @JsonCreator
    public GroupTimer(@JsonProperty("name") String name){
        this.name = name;
        this.delayAfterRepeats = "0,0";
        this.repeats=-1;
        this.threads =1;
    }

    public String getName() {
        return name;
    }

    public GroupTimer setName(String name) {
        this.name = name;
        return this;
    }

    public String getDelayAfterRepeats() {
        return delayAfterRepeats;
    }

    public GroupTimer setDelayAfterRepeats(String delayAfterRepeats) {
        this.delayAfterRepeats = delayAfterRepeats;
        return this;
    }

    public long getRuntime() {
        return runtime;
    }

    public GroupTimer setRuntime(long runtime) {
        this.runtime = runtime;
        return this;
    }

    public int getThreads() {
        return threads;
    }

    public GroupTimer setThreads(int threads) {
        this.threads = threads;
        return this;
    }

    public long getRepeats() {
        return repeats;
    }

    public GroupTimer setRepeats(long repeats) {
        this.repeats = repeats;
        return this;
    }

    public GroupTimer clone() throws CloneNotSupportedException {
        return (GroupTimer) super.clone();
    }

    public GroupTimer setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public GroupTimer incrementRepeatsDone() {
        this.repeatsDone++;
        return this;
    }

    public long getRepeatsDone() {
        return repeatsDone;
    }

    public GroupTimer setRepeatsDone(long repeatsDone) {
        this.repeatsDone = repeatsDone;
        return this;
    }

    public String toString() {
        return this.name +" threads : "+this.threads+" runtime : "+this.runtime+" repeats : "+this.repeats;
    }

}
