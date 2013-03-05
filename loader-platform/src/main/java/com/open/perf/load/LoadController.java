package com.open.perf.load;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import com.open.perf.domain.Group;
import org.apache.log4j.Logger;

import com.open.perf.util.HelperUtil;
import com.open.perf.domain.Groups;

public class LoadController extends Thread{
    private Map<String,List<String>>        groupDependency = new LinkedHashMap<String,List<String>>();
    private Map<String,GroupControllerNew> groupControllersMap = new LinkedHashMap<String,GroupControllerNew>();

    private Map<String,Group> groupMap;
    private static Logger logger;

    static {
        logger =   Logger.getLogger(LoadController.class);
    }

    public LoadController(Groups groupControllersMap) throws Exception {
        this.setName("Thread-LoadController");

        this.groupMap   =   groupControllersMap.getGroups();

        // Validating Cyclic Dependency.
        if(System.getenv("VALIDATE_DEPENDENCY") != null)
            validateCyclicDependency(); //Seems to be becoming expensive

        // Validating Log Folder
        validateLogFolder(groupControllersMap);

        // Creating Groups Base Log Folder
        String groupsFolder    =   groupControllersMap.getLogFolder();
        if(new File(groupsFolder).exists() == false) {
            (new File(groupsFolder)).mkdirs();
            logger.debug("Directory '" + groupsFolder + "' created for all groups logs");
        }

        HashMap<String,Group> groupsMap    =   groupControllersMap.getGroups();

        logger.debug("Number of groups : "+groupsMap.size());
        for(String group : groupsMap.keySet()) {
            Group groupBean       =   groupsMap.get(group);

            // if repeat is mentioned as 0 then it will be treated as -1

            if(groupBean.getRepeats()==0) {
                logger.info("In group '"+groupBean.getName()+"' repeat = 0 has no meaning, changing it to -1");
                groupBean.setRepeats(-1);
            }
            if(groupBean.getDuration()==0) {
                logger.info("In group '"+groupBean.getName()+"' life = 0 has no meaning, changing it to -1");
                groupBean.setDuration(-1);
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
                Group depGroupBean    = groupMap.get(depGroup);
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
    private void validateLogFolder(Groups groups) {
        String groupsLogFolder =   groups.getLogFolder();
        HashMap<String,Group> groupMap =   groups.getGroups();
        for(Group group : groupMap.values()) {
            logger.debug("Original Log Folder for Group '"+ group.getName()+"' is '"+ group.getLogFolder()+"'");
            if(group.getLogFolder().equals("")) { // Not doing null check as default value is Empty only
                logger.debug("Setting it to '"+groupsLogFolder+"'");
                group.setLogFolder(groupsLogFolder + "/" + group.getName());
            }
        }
    }

    public void addGroup(Group group) throws InterruptedException, FileNotFoundException {
        String groupName = group.getName();
        List<String>  dependsOnList   =   group.getDependOnGroups();

        GroupControllerNew    groupController =   new GroupControllerNew(group);

        groupDependency.put(groupName, dependsOnList);
        groupControllersMap.put(groupName, groupController);
    }

    public void run() {
        logger.info("****LOAD CONTROLLER STARTED****");
        long startTime     = System.currentTimeMillis();

        while(true) {
            ArrayList<GroupControllerNew> groupsToRun = new ArrayList<GroupControllerNew>();
            int groupsCanNotStartThisTime = 0;

            for(String group : this.groupControllersMap.keySet()) {
                GroupControllerNew groupController = this.groupControllersMap.get(group);

                if(!groupController.started()) {
                    boolean groupCanRun = true;

                    List<String> dependencyList = this.groupDependency.get(group);
                       if(dependencyList != null) {
                           for(String dependOnGroup : dependencyList) {
                               GroupControllerNew dependOnGroupController = this.groupControllersMap.get(dependOnGroup);

                               if(!dependOnGroupController.started() || dependOnGroupController.isAlive()) {
                                   groupCanRun = false;
                                   break;
                               }
                           }
                       }

                    if(groupCanRun)
                        groupsToRun.add(this.groupControllersMap.get(group));
                    else
                        groupsCanNotStartThisTime++;

                }
            }


            // This will happen only if there are no pending groups to start
            for(GroupControllerNew groupController : groupsToRun) {
                logger.info("******"+HelperUtil.getEqualChars("Running Group [" + groupController.getGroupName() + "]", '*')+"******");
                logger.info("******Running Group ["+groupController.getGroupName()+"]******");
                logger.info("******" + HelperUtil.getEqualChars("Running Group [" + groupController.getGroupName() + "]", '*') + "******");
                try {
                    groupController.start();
                    logger.info("******Group "+groupController.getGroupName()+" Started******");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            groupsToRun.clear();

            if(groupsCanNotStartThisTime == 0 )
                break;

            logger.info("Groups Can not be started :"+groupsCanNotStartThisTime);
            HelperUtil.delay(250);
        }

        waitTillGroupsFinish(this.groupControllersMap.values());
        logger.info("Loader Execution Time :"+(System.currentTimeMillis()-startTime)+" milli seconds");
    }

    private void waitTillGroupsFinish(Collection<GroupControllerNew> groupControllers) {
        while(haveLiveGroups(groupControllers))
            HelperUtil.delay(250);
    }

    private boolean haveLiveGroups(Collection<GroupControllerNew> groupControllers) {
        for(GroupControllerNew grouper : groupControllers) {
            if(grouper.isAlive())
                return true;
            else {
                grouper.stopStatsCollection();
            }
        }
        return false;
    }


    public boolean getRunning() {
        return this.isAlive();
    }


}
