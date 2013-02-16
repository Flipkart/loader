package com.open.perf.load;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.GroupTimer;
import com.open.perf.util.Counter;
import com.open.perf.util.Timer;
import org.apache.log4j.Logger;

import com.json.JSONException;
import com.json.JSONObject;
import com.open.perf.domain.Group;
import com.open.perf.util.HelperUtil;

public class GroupController extends Thread{
    public static final long    DUMP_DELAY              =   360000; // 360 seconds by default
    private long                dumpDelaySlotUsed       =   0;      // used to track dumps done with the usage of DUMP_DELAY   
    private long                lastDumpThread          =   0;      // used to track the last Thread when dump was done. 
    private long                lastDumpTime            =   0;      // used to track the last Thread when dump was done.

    public static final String GROUP_RUNNING = "RUNNING";
    public static final String GROUP_NOT_STARTED = "NOT_STARTED";
    public static final String GROUP_DEAD = "DEAD";

    private String              groupName;
    private long                repeat;
    private long                repeatDone;

    private long                life;  // in ms
    private long                startTime;
    private long                endTime                 =   -1;
    private int                 threads;
    private long                threadsDone;
    private int                 groupStartDelay;
    private String              delayAfterRepeats;
    private int                 threadStartDelay;
    private String              logFolder;

    /*
     * Contains the list of funtions to be executed in sequence for every 
     */
    private ArrayList<String>   functions; // Making it functionName_classFunction to uniquely identify
    private ArrayList<String>   ignoreDumpFunctions;
    private HashMap<String,Object> params;

    /*
     * Various Timers specifically for different functions
     */
    private HashMap<String,List<Float>>functionDumpAvgTime;
    private Map<String, Long> lastFunctionDumpCount;

    private Map<String, FunctionStats> functionStatsMap;

    /*
     * Time taken by particular function between the dumps
     * This will help in calculating intermediate average
     */
    private HashMap<String,Double>    dumpLevelFunctionTime = new HashMap<String,Double>();

    private int                     dumpDataAfterRepeat;
    private ArrayList<CSequentialFunctionExecutor> sfesCons;
    
    // IT will help is controlling the addition and removal of threads
    private ThreadGroup             tg;
    private long repeatStartTime            ;
    private List<GroupFunction> groupFunctions;
    private TimerThread timerThread;
    private List<Map<String,Object>> threadResources;
    private GroupTimer currentTimer;
    private int newThreads = -1;

    private static Logger           logger;
    static {
        logger      = Logger.getLogger(GroupController.class);
    }

    private final Map<String, Timer> functionTimers;
    private final Map<String, Counter> functionCounters;


    // Simplified & readable Constructor using Group
    public GroupController(Group group) throws IOException, MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        this.setName("Thread-GroupController("+ group.getName()+")");
        this.groupName              =   group.getName();
        this.params                 =   group.getParams();
        this.params.put("GROUP_NAME",this.groupName);
        this.repeatDone             =   0;
        this.life                   =   group.getLife();
        this.repeat                 =   group.getRepeats();
        this.dumpDataAfterRepeat    =   group.getDumpDataAfterRepeats();
        this.groupStartDelay        =   group.getGroupStartDelay();
        this.threadStartDelay       =   group.getThreadStartDelay();
        this.logFolder              =   group.getLogFolder();

        // At group Level
        this.delayAfterRepeats      =   group.getDelayAfterRepeats();
        this.threads                =   group.getThreads();
        this.threadResources        =   group.getThreadResources();

        // At Timer Level
        // If user has used Timers, choose the first Time to get started
        if(group.getTimers().size() > 0) {
            GroupTimer firstTimer            =   group.getTimers().get(0);
            this.threads                =   firstTimer.getThreads();
            this.delayAfterRepeats      =   firstTimer.getDelayAfterRepeats();
            this.timerThread            =   new TimerThread(this, group.getTimers());
            this.currentTimer = firstTimer;
            this.repeat = -1l;
        }

        // No Threads
        if(this.threads < 1) {
            throw new RuntimeException("No Threads mentioned for group '"+this.groupName+"'");
        }

        // FIX THE FOLLOWING CODE. SINCE WE DON'T DO TOO MUCH OF WRITING IT'S GOOD TO OPEN THE FILE DESCRIPTOR WHEN NEEDED INSTEAD OF ALWAYS KEEPING IT OPEN
        File logFolder              =   new File(this.logFolder);
        if(logFolder.exists() == false)
            logFolder.mkdirs();

        this.groupFunctions =   group.getFunctions();
        
        this.functions              =   new ArrayList<String>();
        this.ignoreDumpFunctions    =   new ArrayList<String>();
        this.lastFunctionDumpCount  =   new HashMap<String, Long>();
        for(GroupFunction groupFunction :   group.getFunctions()) {
            String uniqueFunctionName = groupFunction.getName()+"_"+ groupFunction.getClassName()+"."+groupFunction.getFunctionName();
            this.functions.add(uniqueFunctionName);
            if(!groupFunction.isDumpData())
                this.ignoreDumpFunctions.add(uniqueFunctionName);

            this.lastFunctionDumpCount.put(uniqueFunctionName, 0l);
        }
        
        logger.debug("Group Name              :"+this.groupName);
        logger.debug("Params                  :"+this.params);
        logger.debug("Life                    :"+this.life);
        logger.debug("Repeats                 :"+this.repeat);
        logger.debug("Threads                 :"+this.threads);
        logger.debug("Dump After Repeats      :"+this.dumpDataAfterRepeat);
        logger.debug("Group Start Delay       :"+this.groupStartDelay);
        logger.debug("Thread Start Delay      :"+this.threadStartDelay);
        logger.debug("Delay After Repeats     :"+this.delayAfterRepeats);
        logger.debug("Ignore Dump             :"+this.ignoreDumpFunctions);
        
        this.functionDumpAvgTime        = new HashMap<String, List<Float>>();
        this.functionStatsMap = new HashMap<String, FunctionStats>();

        for(String function : this.functions) {
            this.dumpLevelFunctionTime.put(function, 0d);
            this.functionDumpAvgTime.put(function, new ArrayList<Float>());
            this.functionStatsMap.put(function, new FunctionStats(function));
        }

        this.functionTimers = createFunctionTimers(group.getFunctionTimers());
        this.functionCounters = createFunctionCounters(group.getFunctionCounters());

        this.tg                 = new ThreadGroup("ThreadGroup-"+this.getName());
    }

    private Map<String, Counter> createFunctionCounters(List<String> functionCounterNames) {
        Map<String, Counter> functionCounters = new HashMap<String, Counter>();
        for(String functionCounterName : functionCounterNames)
            functionCounters.put(functionCounterName, new Counter(functionCounterName));
        return functionCounters;
    }

    private Map<String, Timer> createFunctionTimers(List<String> functionTimerNames) {
        Map<String, Timer> timers = new HashMap<String, Timer>();
        for(String functionTimerName : functionTimerNames)
            timers.put(functionTimerName, new Timer(functionTimerName));
        return timers;
    }

    public void run()  {
        LoadController.updateGroupStatus(this.getGroupName(), GROUP_RUNNING);
        boolean unsuccessful    = false;
        
        this.repeatDone = 0;
        if(this.groupStartDelay > 0) {
            logger.info("Delaying Group Start by '"+this.groupStartDelay+" milli seconds' as mentioned by user");
            HelperUtil.delay(this.groupStartDelay);
        }

        if(this.timerThread != null)
            this.timerThread.start();

        long time               =   System.currentTimeMillis();;
        this.lastDumpTime       =   time;
        this.repeatStartTime    =   time;
        this.startTime          =   time;
        if(this.currentTimer != null)
            this.currentTimer.setStartTime(time);

        this.sfesCons = new ArrayList<CSequentialFunctionExecutor>();
        for(int i=0; i<this.threads; i++) {

            try {
                CSequentialFunctionExecutor sfe = new CSequentialFunctionExecutor(this.groupFunctions, this.params,this.repeat, this.life,
                        this.startTime, this.tg, "CSequentialFunctionExecutor-"+i);
                sfe.setCallBackMethod(this.getClass().getName(), "listener", this, new Class[]{CSequentialFunctionExecutor.class}
                , new Object[]{sfe}, false);
                sfe.setFunctionTimers(this.functionTimers);
                sfe.setFunctionCounters(this.functionCounters);
                HelperUtil.delay(this.threadStartDelay);
                sfe.execute();
                this.sfesCons.add(sfe);
                if(this.threadResources.size() > i) {
                    sfe.setThreadResources(this.threadResources.get(i));
                }
                logger.debug("Threads '"+(i+1)+"' created");

            } catch (NoSuchMethodException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw new RuntimeException(e);
            }
        }
        logger.info("Threads have Started for '"+this.getGroupName()+"'");

        // Wait for threads to get over
        while(groupIsAlive()){
            HelperUtil.delay(1000);
        }

        // Following code is written just to avoid dumping the data in the end if "dumpDataAfterRepeats" had already dumped the data in the end
        boolean dumpStats = false;
        if(this.dumpDataAfterRepeat > 0) {
            if(this.threadsDone % (this.threads*this.dumpDataAfterRepeat) == 0 ) {
                if(this.repeatDone >=1)
                    dumpStats= true;
            }   
        }
        if(dumpStats == false)
            dumpStats();

        try {
            calculatePercentiles();
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        this.endTime = System.currentTimeMillis();
        LoadController.updateGroupStatus(this.getGroupName(), GROUP_DEAD);
        if(unsuccessful)
            throw new RuntimeException("Problem in running the Thread Grouper '"+this.groupName+"'");

        logger.info("******"+HelperUtil.getEqualChars("Group ["+this.groupName+"] Execution Time = "+(this.endTime-this.startTime), '*')+"******");
        logger.info("******Group ["+this.groupName+"] Execution Time = "+(this.endTime-this.startTime)+"******");
        logger.info("******"+HelperUtil.getEqualChars("Group ["+this.groupName+"] Execution Time = "+(this.endTime-this.startTime), '*')+"******");
    }

    private boolean groupIsAlive() {
        synchronized (this.sfesCons){
            for(CSequentialFunctionExecutor csfe : this.sfesCons)
                if(csfe.isAlive())
                    return true;
        }
        return false;
    }

    public String getGroupName() {
        return this.groupName;
    }
    
    public void setThreads(int newThreads) throws NoSuchMethodException, ClassNotFoundException {
        if(this.newThreads == -1) {
            this.newThreads = newThreads;
            return; // Just a hack for the time being as way to simulate receiving request for change threads
        }
        synchronized (this.sfesCons)  {
            if(newThreads <= 0) {
                System.err.println("Can't set 0 <= number of threads. Retaining old '"+this.threads+"' threads.");
            }
            else if(newThreads < this.threads) {
                int reduceThreads = this.threads - newThreads;
                logger.debug(reduceThreads+" threads have to be reduced");
                for(int i=1; i<=reduceThreads; i++) {
                    CSequentialFunctionExecutor sfe = sfesCons.get(sfesCons.size()-i);
                    sfe.forceStop();
                    logger.debug(sfe.getName()+" joined ("+i+" done)");

                }
                for(int i=1; i<=reduceThreads; i++) {
                    sfesCons.remove(sfesCons.size()-1);
                    logger.debug("In List "+sfesCons.size());
                }

                this.threads = newThreads;
            }
            else {
                int increasedThreads = newThreads - this.threads;
                logger.debug(increasedThreads+" threads have to be increased");
                for(int i=0; i<increasedThreads; i++) {
                    CSequentialFunctionExecutor sfe = new CSequentialFunctionExecutor(this.groupFunctions, this.params,this.repeat, this.life
                            ,this.startTime, this.tg, "CSequentialFunctionExecutor-"+(sfesCons.size() + i));

                    sfe.setCallBackMethod(this.getClass().getName(), "listener", this, new Class[]{CSequentialFunctionExecutor.class}
                    , new Object[]{sfe}, false);

                    if(this.threadResources.size() > (this.sfesCons.size() + i)) {
                        sfe.setThreadResources(this.threadResources.get(this.sfesCons.size() + i));
                    }

                    sfe.setFunctionTimers(this.functionTimers);
                    sfe.setFunctionCounters(this.functionCounters);
                    HelperUtil.delay(this.threadStartDelay);

                    sfe.execute();
                    this.sfesCons.add(sfe);
                    logger.debug("Threads '"+(i+1)+"' created");
                }

                this.threads = newThreads;
            }
        }

        logger.debug("Total Threads running "+sfesCons.size()+"(In List) "+this.threads+"(ThreadCount)");
        logger.info("Total Threads running "+sfesCons.size()+"(In List) "+this.threads+"(ThreadCount)");
        this.newThreads = -1;
    }
    
    public long getRunTimeInMilliSeconds() {
        if(this.isAlive())
            return System.currentTimeMillis() - this.startTime;
        else
            return this.endTime - this.startTime;
    }

    public void setDelayAfterRepeats(String newDelayAfterRepeats) {
        try {
            String[] tmp = newDelayAfterRepeats.split(",");
            if(this.delayAfterRepeats.equals(tmp[0]+","+tmp[1])==false)
                this.delayAfterRepeats=newDelayAfterRepeats;
        }
        catch(NumberFormatException nfe) {
            nfe.printStackTrace();
        }       
    }

    // This function is used in call backs
    synchronized public void listener(CSequentialFunctionExecutor sfe) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        long time       =   System.currentTimeMillis();
        logger.debug("In Listener");

        // Capture basic function stats
        for(SyncFunctionExecutor fe : sfe.fExecutors) {
            String functionName = fe.getFunctionalityName()+"_"+fe.getAbsoluteFunctionName();
            FunctionStats functionStats = this.functionStatsMap.get(functionName);
            if(fe.isExecuted()) {
                functionStats.executed();

                if(sfe.getErroredFunctions().contains(fe))
                    functionStats.errored();

                if(sfe.getFailedFunctions().contains(fe))
                    functionStats.failed();


                double executionTime  = fe.getExecutionTime()/1000;
                functionStats.addValue(executionTime);
            }
            else {
                functionStats.skipped();
            }
        }
        List<SyncFunctionExecutor> fes = sfe.getFunctionExecutors();


        this.threadsDone++;
        logger.info(" ops done :"+this.threadsDone);
        if(threadsDone % this.threads == 0) {
            this.repeatDone++;
            if(this.currentTimer != null) {
                this.currentTimer.incrementRepeatsDone();
            }
            logger.debug("Repeats Done "+this.repeatDone);
        }    

        boolean dumpData = false;
        
        if(this.dumpDataAfterRepeat > 0) {
            if(this.threadsDone % (this.threads*this.dumpDataAfterRepeat) == 0 ) {
                if(this.repeatDone >=1)
                    dumpData= true;
            }   
        }
        
        if(dumpData == false) {
            if(((time - this.startTime) / GroupController.DUMP_DELAY) > this.dumpDelaySlotUsed) {
                this.dumpDelaySlotUsed ++;
                dumpData    = true;
            }
        }
        
        if(dumpData) {  
            dumpStats();
        }
        
        // This loop gets executed when one repeat gets over. A repeat is equivalent to the number of threads mentioned for the group
        if(threadsDone % this.threads == 0) {
            int delay   =   0;

            // Add the logic to implement the logic of "generate x load for y duration"
            // This code will be executed only if both life and repeat for the group is not negative
            // Here the delay between the repeat would very dynamically and in ideal situation it would be uniform load generation
            if(this.life    >   0   &&  this.repeat   >   0) {
                logger.debug("Group Performing Uniform Load Logic. This will avoid delay mentioned in 'delayAfterRepeats''");


                long timeSpent              =   time            -   this.startTime;
                long timeLeft               =   this.life       -   timeSpent;
                if(timeLeft >   0) {
                    long    repeatExecutionTime =   time        -   this.repeatStartTime;
                    long    repeatsLeft         =   this.repeat -   this.repeatDone;
                    
                    // Approximately time required to execute remaining repeats.
                    long    timeRequiredForRepeats  =   repeatsLeft *   repeatExecutionTime;
                    long    totalDelayLeft          =   timeLeft    -   timeRequiredForRepeats;
                    
                    if(totalDelayLeft   >   0) {
                        delay   =   (int)totalDelayLeft/(int)(repeatsLeft+1);
                    }
                    logger.debug("Last Repeat Execution Time:"+repeatExecutionTime
                            +", Repeats Left:"+repeatsLeft
                            +", Time Required for Repeats:"+timeRequiredForRepeats
                            +", Total Delay Left:"+totalDelayLeft);
                }

                // Replicate Above Logic   For Timer with Repeats

            }
            else {

                // Following delay is based on user configuration "delayAfterRepeats=$repeats,$delay in milliseconds"
                String[] delayAfterRepeatsValues = delayAfterRepeats.split(",");
                if((Integer.parseInt(delayAfterRepeatsValues[0])) !=0 && (this.repeatDone % Integer.parseInt(delayAfterRepeatsValues[0]) == 0)) {
                    delay   =   Integer.parseInt(delayAfterRepeatsValues[1]);
                }
            }

            // That means we are using timers
            if(this.currentTimer != null) {
                if(this.currentTimer.getRuntime()    >   0   &&  this.currentTimer.getRepeats()   >   0) {
                    logger.debug("Timer - Performing Uniform Load Logic. This will avoid delay mentioned in 'delayAfterRepeats''");

                    long timeSpent              =   time            -   this.currentTimer.getStartTime();
                    long timeLeft               =   this.currentTimer.getRuntime()       -   timeSpent;

                    logger.debug("Timer - Time Left : "+timeLeft);
                    if(timeLeft >   0) {
                        long    repeatExecutionTime =   time -   this.repeatStartTime;
                        long    repeatsLeft         =   this.currentTimer.getRepeats() -   this.currentTimer.getRepeatsDone();

                        // Approximately time required to execute remaining repeats.
                        long    timeRequiredForRepeats  =   repeatsLeft *   repeatExecutionTime;
                        long    totalDelayLeft          =   timeLeft    -   timeRequiredForRepeats;

                        if(totalDelayLeft   >   0) {
                            delay   =   (int)totalDelayLeft/(int)(repeatsLeft+1);
                        }
                        logger.debug("Timer - Last Repeat Execution Time:"+repeatExecutionTime
                                +", Repeats Left:"+repeatsLeft
                                +", Time Required for Repeats:"+timeRequiredForRepeats
                                +", Total Delay Left:"+totalDelayLeft);
                    }
                }
            }

           if(delay > 0)
                logger.debug("Group '"+this.getGroupName()+"' is Sleeping for "+delay+ " milli seconds after "+this.repeatDone+" repeats");

            HelperUtil.delay(delay);
            this.repeatStartTime    =   System.currentTimeMillis();

            logger.debug("Group '"+this.getGroupName()+"' is up now");
        }

        this.setThreads(this.newThreads);

    }
    
    private void dumpStats() {
        
        // Number of threads used for this dump
        long currentTime        = System.currentTimeMillis();
        long dumpTimeInterval   = currentTime - this.lastDumpTime;
        this.lastDumpTime       = currentTime;

        String date = new Date(System.currentTimeMillis()).toString();
        ArrayList<String> functionsPrinted = new ArrayList<String>();

        for(String function : functions) {
            FunctionStats functionStats = this.functionStatsMap.get(function);
            double functionTotalTime = functionStats.getTotalFunctionTime();

            double functionAvg = functionStats.computeAverage();

            if(this.ignoreDumpFunctions.contains(function)) {
                logger.debug("Function '"+function+"' has ignore dump true");
                functionsPrinted.add(function);
                continue;   
            }
            
            if(functionsPrinted.contains(function) == false) {
                double dumpLevelFunctionTime = functionTotalTime - this.dumpLevelFunctionTime.get(function);
                
                this.dumpLevelFunctionTime.put(function, functionTotalTime);

                long functionCountInThisDump = functionStats.getCount().count() - this.lastFunctionDumpCount.get(function);
                this.lastFunctionDumpCount.put(function, functionStats.getCount().count());

                float dumpLevelFunctionAvg = (float)dumpLevelFunctionTime / functionCountInThisDump;
                if(Float.isNaN(dumpLevelFunctionAvg ))
                    dumpLevelFunctionAvg = 0;

                float seconds = dumpLevelFunctionTime == 0 ? 0 : ((float)dumpTimeInterval/1000);
                float throughputPerSecond  = seconds == 0 ? 0 : functionCountInThisDump / seconds;
                float throughputPerMinute  = throughputPerSecond * 60;

                String info = this.groupName+"."
                        + function.split("\\.")[0].replace("_com","")
                        + ": OperationsDone=" + (functionStats.getCount().count())
                        +", Min="+(functionStats.getMin() == Double.MAX_VALUE ? 0 : functionStats.getMin())
                        +", Avg="+functionAvg
                        +", Max="+(functionStats.getMax() == Double.MIN_VALUE ? 0 : functionStats.getMax())
                        +", DumpAvg="+dumpLevelFunctionAvg
                        +", perSecond="+throughputPerSecond
                        +", perMinute="+throughputPerMinute
                        +", Error="+functionStats.getErrorCounter().count()
                        +", Failed="+functionStats.getFailureCounter().count()
                        +", Skipped="+functionStats.getSkipCounter().count()
                        +", THREAD="+this.threads;

                if(System.getenv("ON_SCREEN_STATS") != null)
                    logger.info(info.toString());
                try {
                    JSONObject stat = new JSONObject();
                    stat.put("time",date).
                            put("function", function).
                            put("repeats",this.repeatDone).
                            put("operations",(functionStats.getCount().count())).
                            put("min", (functionStats.getMin() == Double.MAX_VALUE ? 0 : functionStats.getMin())).
                            put("avg",functionAvg).
                            put("dumpAvg",dumpLevelFunctionAvg/1000).
                            put("max",functionStats.getMax() == Double.MIN_VALUE ? 0 : functionStats.getMax()).
                            put("perSecond", throughputPerSecond).
                            put("perMinute",throughputPerMinute).
                            put("errored",functionStats.getErrorCounter().count()).
                            put("failed", functionStats.getFailureCounter().count()).
                            put("skipped", functionStats.getSkipCounter().count()).
                            put("threads", this.threads);

                    // Function Specific Log File
                    BufferedWriter functionWriter   =   new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.logFolder+File.separatorChar+function+".txt",true)));
                    functionWriter.write(stat.toString()+"\n");
                    functionWriter.flush();
                    functionWriter.close();
                    
                } catch (IOException e) {
                    logger.warn(HelperUtil.getExceptionString(e));
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    logger.warn(HelperUtil.getExceptionString(e));
                }
                functionsPrinted.add(function);
                            }
        }
        functionsPrinted.clear();   
    }

    // In Milli Seconds
    private double findPercentile(List<Double> functionDumpAverages, float percentile) {
        int index = (int)(functionDumpAverages.size()  * percentile);
        return functionDumpAverages.get(index);
    }

    public void pause() {
        for(CSequentialFunctionExecutor sfe : this.sfesCons) {
            sfe.pause();
        }
    }

    public void resume0() {
        for(CSequentialFunctionExecutor sfe : this.sfesCons) {
            sfe.resume0();
        }   
    }

    public boolean isPaused() {
        for(CSequentialFunctionExecutor sfe : this.sfesCons) {
            if(sfe.getPaused() == false)
                return false;
        }   
        return true;
    }
    
    public boolean isRunning() {
        for(CSequentialFunctionExecutor sfe : this.sfesCons) {
            if(sfe.getRunning() == false)
                return false;
        }   
        return true;    
    }

    public boolean isDead() {
        return !this.isAlive();
    }

    public void calculatePercentiles() throws JSONException, IOException {
        synchronized (this.functionStatsMap) {
            for(String functionName : this.functionStatsMap.keySet()) {
                FunctionStats functionStats = this.functionStatsMap.get(functionName);
                if(!this.ignoreDumpFunctions.contains(functionName)) {
                    List<Double> functionExecutionTimes = functionStats.getValues();
                    if(functionExecutionTimes.size() > 0) {
                        Collections.sort(functionExecutionTimes);
                        String date = new Date(System.currentTimeMillis()).toString();
                        double fiftyPercentile = findPercentile(functionExecutionTimes, .50f);
                        double seventyFifthPercentile = findPercentile(functionExecutionTimes, .75f);
                        double ninetiethPercentile = findPercentile(functionExecutionTimes, .90f);
                        double ninetyFifthPercentile = findPercentile(functionExecutionTimes, .95f);
                        double ninetyEigthPercentile = findPercentile(functionExecutionTimes, .98f);
                        double ninetyNinthPercentile = findPercentile(functionExecutionTimes, .99f);

                        JSONObject stat = new JSONObject();
                        stat.put("function", functionName).
                                put("time",date).
                                put("fiftyPercentile", fiftyPercentile).
                                put("seventyFifthPercentile", seventyFifthPercentile).
                                put("ninetiethPercentile", ninetiethPercentile).
                                put("ninetyFifthPercentile",ninetyFifthPercentile).
                                put("ninetyEigthPercentile",ninetyEigthPercentile).
                                put("ninetyNinthPercentile",ninetyNinthPercentile);

                        if(System.getenv("ON_SCREEN_STATS") != null)
                            logger.info(this.getGroupName()+"."+functionName.split("\\.")[0].replace("_com","") + ": 50th : "+fiftyPercentile
                                    + ", 75th : "+seventyFifthPercentile
                                    + ", 90th : "+ninetiethPercentile
                                    + ", 95th : "+ninetyFifthPercentile
                                    + ", 98th : "+ninetyEigthPercentile
                                    + ", 99th : "+ninetyNinthPercentile );

                        // Function Specific Log File
                        BufferedWriter functionWriter   = null;
                            functionWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.logFolder+ File.separatorChar+functionName+"_percentiles.txt",true)));
                            functionWriter.write(stat.toString() + "\n");
                            functionWriter.flush();
                            functionWriter.close();
                    }
                }
            }
        }

    }

    public void setCurrentTime(GroupTimer currentTimer) {
        this.currentTimer = currentTimer;
    }
}
