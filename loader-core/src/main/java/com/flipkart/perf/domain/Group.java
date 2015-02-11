package com.flipkart.perf.domain;

import com.flipkart.perf.datagenerator.DataGeneratorInfo;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.*;

public class Group {

    private String name ;
    private int groupStartDelay ;
    private int threadStartDelay ;

    private static final int DEFAULT_DURATION = 5 * 24 * 60 * 60 * 1000;
    private static int noOfProcessor;
    private static final int DEFAULT_THROUGHPUT;
    private float throughput;
    private long repeats ;
    private long duration;
    private int threads ;
    private int warmUpRepeats ;

    private List<GroupFunction> functions;
    private List<String> dependOnGroups;
    private HashMap<String, Object> params;
    private List<GroupTimer> timers;

    // Resources (params) that could be passed to specific thread in the group
    private List<Map<String,Object>> threadResources ;
    private List<String> customTimers;
    private List<String> customCounters;
    private Map<String, DataGeneratorInfo> dataGenerators;

    static {
        noOfProcessor = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getAvailableProcessors();
        DEFAULT_THROUGHPUT = (noOfProcessor / 2) * 7500;  //15000; // per second
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
        this.warmUpRepeats = -1;
        this.customTimers = new ArrayList<String>();
        this.customCounters = new ArrayList<String>();
        this.dataGenerators = new HashMap<String, DataGeneratorInfo>();
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

    public int getWarmUpRepeats() {
        return warmUpRepeats;
    }

    public Group setWarmUpRepeats(int warmUpRepeats) {
        this.warmUpRepeats = warmUpRepeats;
        return this;
    }

    public List<Map<String, Object>> getThreadResources() {
        return threadResources;
    }

    public Map<String,Object> getThreadResources(int threadNumber) {
        return this.threadResources.get(threadNumber);
    }

    public Group addThreadResource(int threadNumber, String resource, Object value) {
        this.threadResources.get(threadNumber).put(resource, value);
        return this;
    }

    void validate() {
        // No Threads
        if(this.threads < 1) {
            throw new RuntimeException("Group "+this.name+": No Threads mentioned for group");
        }

        // Resetting repeat or duration if needed
        if(this.repeats==0) {
            this.repeats = -1;
        }
        if(this.duration==0) {
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

    public Map<String, DataGeneratorInfo> getDataGenerators() {
        return dataGenerators;
    }

    public Group setDataGenerators(Map<String, DataGeneratorInfo> dataGenerators) {
        this.dataGenerators = dataGenerators;
        return this;
    }

    public Group addDataGenerator(DataGeneratorInfo dataGeneratorInfo) {
        this.dataGenerators.put(dataGeneratorInfo.getGeneratorName(), dataGeneratorInfo);
        return this;
    }

}
