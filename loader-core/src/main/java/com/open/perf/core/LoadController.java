package com.open.perf.core;

import com.open.perf.domain.Group;
import com.open.perf.domain.Load;
import com.open.perf.util.Clock;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * Entry point of Load Generation.
 * This Thread Creates Group Controller resolving Group Dependencies on the way.
 */
public class LoadController extends Thread{
    // Map of group and list of dependent groups
    private Map<String,List<String>> groupDependency = new LinkedHashMap<String,List<String>>();

    // Map of Group Name and Actual Group Controller Instance
    private Map<String,GroupController> groupControllersMap = new LinkedHashMap<String,GroupController>();

    // Map of Group Name and Group Bean (Which contains user information)
    private Map<String,Group> groupMap;

    private static Logger logger = Logger.getLogger(LoadController.class);

    private final String jobId;

    public LoadController(String jobId, Load load) throws Exception {
        this.setName("Thread-LoadController");
        this.jobId = jobId;

        this.groupMap   =   load.groupMap();
        logger.info(jobId+" Number of groups : "+this.groupMap.size());

        validateCyclicDependency(); //Seems to be becoming expensive

        for(Group group : this.groupMap.values()) {
            this.addGroup(group);
        }
    }

    /**
     * Creating Group Dependency and Group Controller for given group
     * @param group
     * @throws InterruptedException
     * @throws FileNotFoundException
     */
    private void addGroup(Group group) throws InterruptedException, FileNotFoundException {
        GroupController groupController =   new GroupController(this.jobId, group);
        groupDependency.put(group.getName(), group.getDependOnGroups());
        groupControllersMap.put(group.getName(), groupController);
    }

    /**
     * Validate cyclic dependencies in the group
     */
    private void validateCyclicDependency() {
        for(String group : groupMap.keySet()) {
            String dependencyGraph  =   getDependencyGraph(group);
            logger.info(jobId+" Dependency graph for '"+group+"' is '"+dependencyGraph+"'");
            String[] dependencies   =   dependencyGraph.split("->");
            if(dependencies.length > 1)
                if(dependencies[dependencies.length-1].trim().equals(dependencies[dependencies.length-2].trim())
                        ||dependencies[dependencies.length-1].trim().equals(dependencies[0].trim()))
                    throw new RuntimeException(jobId+" Cyclic Dependency '"+dependencyGraph+"' for group '"+group+"'");

        }
    }

    private String validateCyclicDependency(String group, List<String> dependOnGroups, String dependencyFlow) throws Exception{
        if(dependOnGroups.size() > 0) {
            for(String depGroup : dependOnGroups) {
                dependencyFlow  +=  " -> "+depGroup;
                Group depGroupBean    = groupMap.get(depGroup);
                if(depGroupBean == null)
                    throw new RuntimeException(jobId+" Group '"+depGroup+"' doesn't exist!!!");
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

    private String getDependencyGraph(String group) {
        String dependencyGraph  = group;
        try {
            dependencyGraph = validateCyclicDependency(group, this.groupMap.get(group).getDependOnGroups(), dependencyGraph);
        } catch (Exception e) {
            dependencyGraph = e.getLocalizedMessage();
        }
        return dependencyGraph;
    }

    /**
     * Function that starts the load generation
     */
    public void run() {
        logger.info("****"+jobId+" LOAD CONTROLLER STARTED****");
        long startTime = Clock.milliTick();

        while(true) {
            ArrayList<GroupController> groupsToRun = new ArrayList<GroupController>();
            int groupsCanNotStartThisTime = 0;

            for(String group : this.groupControllersMap.keySet()) {
                GroupController groupController = this.groupControllersMap.get(group);

                if(!groupController.started()) {
                    boolean groupCanRun = true;

                    List<String> dependencyList = this.groupDependency.get(group);
                    if(dependencyList != null) {
                        for(String dependOnGroup : dependencyList) {
                            GroupController dependOnGroupController = this.groupControllersMap.get(dependOnGroup);

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

            // Start Group Controllers which can be started
            for(GroupController groupController : groupsToRun) {
                logger.info("******"+jobId+" Running Group [" + groupController.getGroupName() + "]"+"******");

                try {
                    groupController.start();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            groupsToRun.clear();

            if(groupsCanNotStartThisTime == 0 )
                break;

            logger.debug(jobId + " Groups Can not be started :" + groupsCanNotStartThisTime);
            try {
                Clock.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // By now there is no group pending to get started
        waitTillGroupsFinish(this.groupControllersMap.values());
        logger.info(jobId+" Loader Execution Time :" + (System.currentTimeMillis() - startTime) + " milli seconds");
    }

    private void waitTillGroupsFinish(Collection<GroupController> groupControllers) {
        while(haveLiveGroups(groupControllers))
            try {
                Clock.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    private boolean haveLiveGroups(Collection<GroupController> groupControllers) {
        boolean stillHaveAliveGroups = false;
        List<GroupController> deadGroups = new ArrayList<GroupController>();
        for(GroupController grouper : groupControllers) {
            if(grouper.isAlive())
                stillHaveAliveGroups = true;
            else {
                deadGroups.add(grouper);
                logger.info("************Group Controller "+grouper.getGroupName()+" Ended**************");
                grouper.stopStatsCollection();
            }
        }
        groupControllers.removeAll(deadGroups);
        return stillHaveAliveGroups;
    }
}
