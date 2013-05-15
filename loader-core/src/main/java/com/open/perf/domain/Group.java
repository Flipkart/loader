package com.open.perf.domain;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

    private static Logger        logger ;

    private String name ;
    private int groupStartDelay ;
    private int threadStartDelay ;

    private static final int DEFAULT_DURATION = 5 * 24 * 60 * 60 * 1000;
    private static final int DEFAULT_THROUGHPUT = 15000; // per second
    private float throughput;
    private long repeats ;
    private long duration;
    private int threads ;
    private int warmUpTime ;
    private int warmUpRepeats ;

    private List<GroupFunction> functions;
    private List<String> dependOnGroups;
    private HashMap<String, Object> params;
    private List<GroupTimer> timers;

    // Resources (params) that could be passed to specific thread in the group
    private List<Map<String,Object>> threadResources ;
    private List<String> customTimers;
    private List<String> customCounters;

    static {
        logger = Logger.getLogger(Group.class.getName());
    }

    public Group(){
        this.functions = new ArrayList<GroupFunction>();
        this.dependOnGroups = new ArrayList<String>();
        this.params = new HashMap<String, Object>();
        this.timers = new ArrayList<GroupTimer>();
        this.threadResources = new ArrayList<Map<String, Object>>();
        this.groupStartDelay = 0;
        this.threadStartDelay = 0;
        this.duration = -1;
        this.throughput = -1;
        this.repeats =  -1;
        this.threads = 1;
        this.warmUpTime = -1;
        this.warmUpRepeats = -1;
        this.customTimers = new ArrayList<String>();
        this.customCounters = new ArrayList<String>();
    }

    public Group (String name) {
        this();
        this.name = name;
    }
    public Group setGroupStartDelay(int groupStartDelay) {
        this.groupStartDelay = groupStartDelay;
        return this;
    }

    public Group setRepeats(int repeats) {
        this.repeats = repeats;
        return this;
    }

    public Group setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public Group setThreads(int threads) {
        this.threadResources.clear();
        this.threads = threads;
        for(int i=0;i<threads;i++)
            threadResources.add(i, new HashMap<String, Object>());
        return this;
    }

    public Group setThreadStartDelay(int threadStartDelay) {
        this.threadStartDelay = threadStartDelay;
        return this;
    }

    public Group addFunction(GroupFunction groupFunction) {
        this.functions.add(groupFunction);
        return this;
    }

    public String getName() {
        return name;
    }

    public int getGroupStartDelay() {
        return groupStartDelay;
    }

    public long getRepeats() {
        return repeats;
    }

    public long getDuration() {
        return duration;
    }

    public int getThreads() {
        return threads;
    }

    public int getThreadStartDelay() {
        return threadStartDelay;
    }

    public List<GroupFunction> getFunctions() {
        return functions;
    }

    public Group setFunctions(List<GroupFunction> functions) {
        this.functions = functions;
        return this;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    public Group dependsOn(String dependsOnGroup) {
        this.dependOnGroups.add(dependsOnGroup);
        return this;
    }

    public List<String> getDependOnGroups() {
        return dependOnGroups;
    }

    public Group addParam(String param, String value) {
        this.params.put(param, value);
        return this;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public Group addTimer(GroupTimer timer) {
        this.timers.add(timer);
        return this;
    }

    public List<GroupTimer> getTimers() {
        return timers;
    }

    public int getWarmUpTime() {
        return warmUpTime;
    }

    public Group setWarmUpTime(int warmUpTime) {
        this.warmUpTime = warmUpTime;
        return this;
    }

    public int getWarmUpRepeats() {
        return warmUpRepeats;
    }

    public Group setWarmUpRepeats(int warmUpRepeats) {
        this.warmUpRepeats = warmUpRepeats;
        return this;
    }

    public boolean needsWarmUp() {
        return this.warmUpRepeats != -1 || this.warmUpRepeats != -1;
    }

    public Group createWarmUpGroup() throws CloneNotSupportedException {
        Group warmUpGroup = new Group("WarmUp"+this.getName());
        warmUpGroup.setGroupStartDelay(this.groupStartDelay).
                setThreadStartDelay(this.threadStartDelay).
                setRepeats(this.warmUpRepeats).
                setDuration(this.warmUpTime).
                setThreads(this.threads);


        logger.debug("Adding group function to warmUp group");
        for(GroupFunction originalFunction : this.functions) {
            GroupFunction clonedFunction = originalFunction.clone();
            warmUpGroup.addFunction(clonedFunction);
        }

        logger.debug("Adding Dependency to warmUp group");
        for(String dependsOn : this.dependOnGroups) {
            warmUpGroup.dependsOn(dependsOn);
        }

        logger.debug("Adding Params");
        for(String key : this.params.keySet()) {
            warmUpGroup.addParam(key, this.params.get(key).toString());
        }
        return warmUpGroup;
    }

    public List<Map<String, Object>> getThreadResources() {
        return threadResources;
    }

    public Map<String,Object> getThreadResources(int threadNumber) {
        return this.threadResources.get(threadNumber);
    }

    public Group addThreadResource(int threadNumber, String resource, Object value) {
        logger.debug("Putting Resource " + resource + " " + value + " for thread " + threadNumber);
        this.threadResources.get(threadNumber).put(resource, value);
        return this;
    }


    public Group addFunctionTimer(String timerName) {
        this.customTimers.add(timerName);
        return this;
    }

    public List<String> getCustomTimers() {
        return customTimers;
    }

    public List<String> getCustomCounters() {
        return customCounters;
    }

    public Group addFunctionCounter(String counterName) {
        this.customCounters.add(counterName);
        return this;
    }

    void validate() {
        // No Threads
        if(this.threads < 1) {
            throw new RuntimeException("Group "+this.name+": No Threads mentioned for group");
        }

        // Resetting repeat or duration if needed
        if(this.repeats==0) {
            logger.info("In group '"+this.name+"' repeat = 0 has no meaning, changing it to -1");
            this.repeats = -1;
        }
        if(this.duration==0) {
            logger.info("In group '"+this.name+"' life = 0 has no meaning, changing it to -1");
            this.duration = -1;
        }

        // Framework doesn't allow throughput more than certain limit and hence throttling here
        if(throughput > DEFAULT_THROUGHPUT)
            this.throughput = DEFAULT_THROUGHPUT;

        if(throughput == -1)
            this.throughput = DEFAULT_THROUGHPUT;

        for(GroupFunction gp : this.functions) {
            gp.validate();
        }
    }

    public Group setThroughput(float throughput) {
        this.throughput = throughput;
        return this;
    }

    public float getThroughput() {
        return throughput;
    }

    public String asString() {
        System.out.println("Collecting Group info");
        return "\nGroup Name "+this.name+" " +
                "\ngroupStartDelay "+this.groupStartDelay+" " +
                "\nthreadsStartDelay "+this.threadStartDelay+"" +
                "\nrepeats "+this.repeats+"" +
                "\nduration "+this.duration +"" +
                "\nthreads "+this.threads+"";
    }

    public static void main(String[] args) {
        Group group = new Group("").setDuration(60000);
        group.validate();
        System.out.println(group.repeats);
        System.out.println(group.duration);
        System.out.println(group.throughput);
    }
}
