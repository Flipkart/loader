package com.open.perf.load;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import org.apache.log4j.Logger;

import com.open.perf.common.HelperUtil;
import com.open.perf.domain.GroupBean;
import com.open.perf.domain.GroupsBean;

public class LoadController extends Thread{
    private Map<String,List<String>>        groupDependency = new LinkedHashMap<String,List<String>>();
    private Map<String,GroupController>     groups = new LinkedHashMap<String,GroupController>();
    private static Map<String,String> groupsStatus = new LinkedHashMap<String,String>();

    private Map<String,GroupBean> groupMap;
    private static Logger logger;

    static {
        logger =   Logger.getLogger(LoadController.class);
    }

    public LoadController(GroupsBean groups) throws Exception {
        this.setName("Thread-LoadController");

        this.groupMap   =   groups.getGroups();

        // Validating Cyclic Dependency.
        if(System.getenv("VALIDATE_DEPENDENCY") != null)
            validateCyclicDependency(); //Seems to be becoming expensive

        // Validating Log Folder
        validateLogFolder(groups);

        // Creating Groups Base Log Folder
        String groupsFolder    =   groups.getLogFolder();
        if(new File(groupsFolder).exists() == false) {
            (new File(groupsFolder)).mkdirs();
            logger.debug("Directory '" + groupsFolder + "' created for all groups logs");
        }

        HashMap<String,GroupBean> groupsMap    =   groups.getGroups();

        logger.debug("Number of groups : "+groupsMap.size());
        for(String group : groupsMap.keySet()) {
            GroupBean groupBean       =   groupsMap.get(group);

            // if repeat is mentioned as 0 then it will be treated as -1

            if(groupBean.getRepeats()==0) {
                logger.info("In group '"+groupBean.getName()+"' repeat = 0 has no meaning, changing it to -1");
                groupBean.setRepeats(-1);
            }
            if(groupBean.getLife()==0) {
                logger.info("In group '"+groupBean.getName()+"' life = 0 has no meaning, changing it to -1");
                groupBean.setLife(-1);
            }

            this.addGroup(groupBean);

        }
    }

    public void validateCyclicDependency() {
        for(String group : groupMap.keySet()) {
            String dependencyGraph  =   getDependencyGraph(group);
            logger.info("Depency graph for '"+group+"' is '"+dependencyGraph+"'");
            String[] dependencies   =   dependencyGraph.split("->");
            if(dependencies.length > 1)
                if(dependencies[dependencies.length-1].trim().equals(dependencies[dependencies.length-2].trim())
                        ||dependencies[dependencies.length-1].trim().equals(dependencies[0].trim()))
                    throw new RuntimeException("Cyclic Dependency '"+dependencyGraph+"' for group '"+group+"'");

        }
    }

    private String validateCyclicDependency(String group, List<String> dependOnGroups, String dependencyFlow) throws Exception{
        if(dependOnGroups.size() > 0) {
            for(String depGroup : dependOnGroups) {
                dependencyFlow  +=  " -> "+depGroup;
                GroupBean depGroupBean    = groupMap.get(depGroup);
                if(depGroupBean == null)
                    throw new RuntimeException("Group '"+depGroup+"' doesn't exist!!!");
                else {
                    // Following Code Can catch Transitive and immediate dependency
                    if(depGroup.equals(group))
                        throw new RuntimeException(dependencyFlow);
                    // Following code will check first level dependency
                    if(this.groupMap.get(depGroup).getDependOnGroups().contains(depGroup)){
                        throw new RuntimeException(dependencyFlow+" -> "+depGroup);
                    }
                }
                // Following Code will help in checking transitive dependency
                if(this.groupMap.get(depGroup).getDependOnGroups().size() > 0) {
                    validateCyclicDependency(group, this.groupMap.get(depGroup).getDependOnGroups(),dependencyFlow);
                }

            }
        }
        return dependencyFlow;
    }

    public String getDependencyGraph(String group) {
        String dependencyGraph  = group;
        try {
            dependencyGraph = validateCyclicDependency(group, this.groupMap.get(group).getDependOnGroups(), dependencyGraph);
        } catch (Exception e) {
            dependencyGraph = e.getLocalizedMessage();
        }
        return dependencyGraph;
    }

    /**
     * In case logFolder is not mentioned at Group level then this function will make use of LogFolder mentioned at Loader level and
     * pass it to Groups also
     */
    private void validateLogFolder(GroupsBean groups) {
        String groupsLogFolder =   groups.getLogFolder();
        HashMap<String,GroupBean> groupMap =   groups.getGroups();
        for(GroupBean group : groupMap.values()) {
            logger.debug("Original Log Folder for Group '"+ group.getName()+"' is '"+ group.getLogFolder()+"'");
            if(group.getLogFolder().equals("")) { // Not doing null check as default value is Empty only
                logger.debug("Setting it to '"+groupsLogFolder+"'");
                group.setLogFolder(groupsLogFolder + "/" + group.getName());
            }
        }
    }

    public void addGroup(GroupBean group) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, NullPointerException, IOException {
        String groupName = group.getName();
        List<String>  dependsOnList   =   group.getDependOnGroups();

        GroupController    groupController =   new GroupController(group);

        groupDependency.put(groupName, dependsOnList);
        groups.put(groupName, groupController);
        groupsStatus.put(groupName, GroupController.GROUP_NOT_STARTED);
    }

    public static void updateGroupStatus(String groupName, String status) {
        groupsStatus.put(groupName, status);
    }

    public void run() {
        logger.info("****GROUP CONTROLLER STARTED****");
        long startTime     = System.currentTimeMillis();
        PercentileCalculatorThread pct = new PercentileCalculatorThread(groups.values());
        pct.start();

        ArrayList<GroupController> groupControllers = new ArrayList<GroupController>();
        while(true) {
            ArrayList<GroupController> groupsToRun = new ArrayList<GroupController>();
            int groupsCanNotStartThisTime = 0;
            for(String group : groupsStatus.keySet()) {
                if(groupsStatus.get(group) == GroupController.GROUP_NOT_STARTED) {
                    boolean canExecute = true;
                    List<String> dependencyList = groupDependency.get(group);
                    if(dependencyList != null) {
                        for(String dependGroup : dependencyList) {
                            if(groupsStatus.get(dependGroup) == GroupController.GROUP_NOT_STARTED ||
                                    groupsStatus.get(dependGroup) == GroupController.GROUP_RUNNING) {
                                canExecute = false;
                                break;
                            }
                        }
                    }
                    if(canExecute)
                        groupsToRun.add(groups.get(group));
                    else
                        groupsCanNotStartThisTime++;
                }
            }

            // This will happen only if there are no pending groups to start
            for(GroupController grouper : groupsToRun) {
                logger.info("******"+HelperUtil.getEqualChars("Running Group [" + grouper.getGroupName() + "]", '*')+"******");
                logger.info("******Running Group ["+grouper.getGroupName()+"]******");
                logger.info("******" + HelperUtil.getEqualChars("Running Group [" + grouper.getGroupName() + "]", '*') + "******");
                grouper.start();
                groupControllers.add(grouper);
            }

            groupsToRun.clear();

            if(groupsCanNotStartThisTime == 0 )
                break;

            HelperUtil.delay(250);
        }

        waitTillGroupsFinish(groupControllers);

        pct.stop();
        logger.debug("Groups Status : "+ groupsStatus.toString());
        logger.info("Loader Execution Time :"+(System.currentTimeMillis()-startTime)+" milli seconds");
    }

    private void waitTillGroupsFinish(ArrayList<GroupController> groupControllers) {
        while(haveLiveGroups(groupControllers))
            HelperUtil.delay(250);
    }

    private boolean haveLiveGroups(ArrayList<GroupController> groupControllers) {
        for(GroupController grouper : groupControllers) {
            if(grouper.isAlive())
                return true;
        }
        return false;
    }

    public boolean getPaused() {
        for(GroupController groupController : this.groups.values()) {
            if(groupController.isDead())
                continue;
            if(groupController.isPaused() == false)
                return false;
        }
        return true;
    }

    public boolean getRunning() {
        return this.isAlive();
    }

    public void pause() {
        for(GroupController groupController : this.groups.values()) {
            if(groupController.isDead())
                continue;
            groupController.pause();
        }
    }

    public void resume0() {
        for(GroupController groupController : this.groups.values()) {
            if(groupController.isDead())
                continue;
            groupController.resume0();
        }
    }

    public String getPausedGroups() {
        List<String> pausedGroups   =   new ArrayList<String>();
        for(String group    : this.groups.keySet()) {
            GroupController groupController =   this.groups.get(group);
            if(groupController.isDead())
                continue;
            if(groupController.isPaused())
                pausedGroups.add(group);
        }
        return pausedGroups.toString();
    }

    public String getDeadGroups() {
        List<String> deadGroups   =   new ArrayList<String>();
        for(String group    : this.groups.keySet()) {
            GroupController groupController =   this.groups.get(group);
            if(groupController.isDead())
                deadGroups.add(group);
        }
        return deadGroups.toString();
    }

    public String getRunningGroups() {
        List<String> runningGroups   =   new ArrayList<String>();
        for(String group    : this.groups.keySet()) {
            GroupController groupController =   this.groups.get(group);
            if(groupController.isDead())
                continue;
            if(groupController.isRunning())
                runningGroups.add(group);
        }
        return runningGroups.toString();
    }
}
