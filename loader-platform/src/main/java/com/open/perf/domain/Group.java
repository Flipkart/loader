package com.open.perf.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Group {
	
	private static Logger        logger ;
	
	@JsonProperty
	private String name ;
	@JsonProperty
    private String graphTab ;
	@JsonProperty
    private int groupStartDelay ;
	@JsonProperty
    private int dumpDataAfterRepeats ;
	@JsonProperty
    private int threadStartDelay ;
	@JsonProperty
    private int repeats ;
	@JsonProperty
    private boolean repeatIsSet ;
	@JsonProperty
    private int life ;
	@JsonProperty
    private int threads ;
	@JsonProperty
    private String delayAfterRepeats ;
	@JsonProperty
    private int warmUpTime ;
	@JsonProperty
    private int warmUpRepeats ;

    private boolean slowLogsEnabled ;
    private String logFolder;

    private List<GroupFunction> functions;
    private List<String> dependOnGroups;
    private HashMap<String, Object> params;
    private List<GroupTimer> timers;

    // threadResources don't work with Timers. Make sure you don't use them
    private List<Map<String,Object>> threadResources ;
    private List<String> functionTimers;
    private List<String> functionCounters;

    @JsonCreator
    public Group(@JsonProperty("name") String name){
    	  this.name = name;
    	  functions = new ArrayList<GroupFunction>();
    	  dependOnGroups = new ArrayList<String>();
    	  params = new HashMap<String, Object>();
    	  timers = new ArrayList<GroupTimer>();
    	  threadResources = new ArrayList<Map<String, Object>>();
    	  groupStartDelay=0;
    	  dumpDataAfterRepeats=10;
    	  threadStartDelay=100;
    	  repeats=1;
    	  repeatIsSet=false;
    	  life=-1;
    	  threads=1;
    	  delayAfterRepeats="0,0";
    	  warmUpTime=-1;
    	  warmUpRepeats=-1;
    	  slowLogsEnabled=false;
    	  logFolder="";   	  
    	  logger = Logger.getLogger(Group.class.getName());
        this.functionTimers = new ArrayList<String>();
        this.functionCounters = new ArrayList<String>();
      }

    public Group setGroupStartDelay(int groupStartDelay) {
        this.groupStartDelay = groupStartDelay;
        return this;
    }

    public Group setRepeats(int repeats) {
        this.repeats = repeats;
        this.repeatIsSet = true;
        return this;
    }

    public Group setLife(int life) {
        this.life = life;
        if(!this.repeatIsSet)
            this.repeats = -1;
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

    public Group setDelayAfterRepeats(int repeats, int delay) {
        this.delayAfterRepeats = repeats+","+delay;
        return this;
    }

    public Group addFunction(GroupFunction groupFunction) {
        this.functions.add(groupFunction);
        groupFunction.setStatFile(this.getLogFolder() + "/" + groupFunction.getName() + "_" + groupFunction.getClassName() + "." + groupFunction.getFunctionName() + ".txt");
        groupFunction.setPercentileStatFile(this.getLogFolder() + "/" + groupFunction.getName() + "_" + groupFunction.getClassName() + "." + groupFunction.getFunctionName() + "_percentiles.txt");
        return this;
    }

    public String getName() {
        return name;
    }

    public int getGroupStartDelay() {
        return groupStartDelay;
    }

    public int getRepeats() {
        return repeats;
    }

    public int getLife() {
        return life;
    }

    public int getThreads() {
        return threads;
    }

    public int getThreadStartDelay() {
        return threadStartDelay;
    }

    public String getDelayAfterRepeats() {
        return delayAfterRepeats;
    }

    public List<GroupFunction> getFunctions() {
        return functions;
    }

    public int getDumpDataAfterRepeats() {
        return dumpDataAfterRepeats;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    public Group setDelayAfterRepeats(String delayAfterRepeats) {
        this.delayAfterRepeats = delayAfterRepeats;
        return this;
    }

    public Group setDumpDataAfterRepeats(int dumpDataAfterRepeats) {
        this.dumpDataAfterRepeats = dumpDataAfterRepeats;
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
        this.repeats = -1; // As such repeats has no meaning with Timers. but not making repeat = -1 is causing synchronization issues.
        return this;
    }

    public List<GroupTimer> getTimers() {
        return timers;
    }

    public Group setLogFolder(String value) {
        if(value != null && value.equals(""))
            throw new RuntimeException("Loader/Groups/Group/LogFolder Can't be Empty!!!!");
        this.logFolder  =   value;
        return this;
    }

    public String getLogFolder() {
        return this.logFolder;
    }

    public boolean isSlowLogsEnabled() {
        return this.slowLogsEnabled;
    }

    public Group enableSlowlogs() {
        this.slowLogsEnabled =   true;
        return this;
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
        Group warmUpGroup = new Group("Warm Up "+this.getName());
        warmUpGroup.setGroupStartDelay(this.groupStartDelay).
                setDumpDataAfterRepeats(this.dumpDataAfterRepeats).
                setThreadStartDelay(this.threadStartDelay).
                setRepeats(this.warmUpRepeats).
                setLife(this.warmUpTime).
                setThreads(this.threads).
                setDelayAfterRepeats(this.delayAfterRepeats);


         logger.info("Adding group function to warmUp group");
         logger.info(this.functions);
         for(GroupFunction originalFunction : this.functions) {
             GroupFunction clonedFunction = originalFunction.clone();
             clonedFunction.doNotGraphIt();
             warmUpGroup.addFunction(clonedFunction);
         }

         logger.info("Adding Dependency to warmUp group");
         for(String dependsOn : this.dependOnGroups) {
             warmUpGroup.dependsOn(dependsOn);
         }

         logger.info("Adding Params");
         for(String key : this.params.keySet()) {
             warmUpGroup.addParam(key, this.params.get(key).toString());
         }
         return warmUpGroup;
   }

    public String asString() {
        System.out.println("Collecting Group info");
        return "\nGroup Name "+this.name+" " +
                "\ngroupStartDelay "+this.groupStartDelay+" " +
                "\ndumpDataAfterRepeats "+this.dumpDataAfterRepeats+"" +
                "\nthreadsStartDelay "+this.threadStartDelay+"" +
                "\nrepeats "+this.repeats+"" +
                "\nrepeatIsSet "+this.repeatIsSet+"" +
                "\nlife "+this.life+"" +
                "\nthreads "+this.threads+"" +
                "\ndelayAfterRepeats "+this.delayAfterRepeats;
    }

    public String getGraphTab() {
        return graphTab;
    }

    public Group setGraphTab(String graphTab) {
        this.graphTab = graphTab;
        return this;
    }

    public List<Map<String, Object>> getThreadResources() {
        return threadResources;
    }

    public Map<String,Object> getThreadResources(int threadNumber) {
        return this.threadResources.get(threadNumber);
    }

    public Group addThreadResource(int threadNumber, String resource, Object value) {
        System.out.println("Putting Resource "+resource+" "+value+" for thread "+threadNumber);
        this.threadResources.get(threadNumber).put(resource, value);
        return this;
    }


    public Group addFunctionTimer(String timerName) {
        this.functionTimers.add(timerName);
        return this;
    }

    public List<String> getFunctionTimers() {
        return functionTimers;
    }

    public Group setFunctionTimers(ArrayList<String> functionTimers) {
        this.functionTimers = functionTimers;
        return this;
    }

    public List<String> getFunctionCounters() {
        return functionCounters;
    }

    public void setFunctionCounters(List<String> functionCounters) {
        this.functionCounters = functionCounters;
    }

    public Group addFunctionCounter(String counterName) {
        this.functionCounters.add(counterName);
        return this;
    }

}
