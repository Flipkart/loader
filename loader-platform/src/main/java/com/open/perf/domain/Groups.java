package com.open.perf.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;


public class Groups {
	
	@JsonProperty("groupList")
	private List<Group> groupList;
	private HashMap<String,Group> groupMap;
    private String logFolder;
    private static Logger logger;
    static {
        logger = Logger.getLogger(Groups.class);
    }

    @JsonCreator
    public Groups() {
    	logger.info("Created a blank groupsbean.");
        groupMap    =   new HashMap<String,Group>();
        groupList = new ArrayList<Group>();
        logFolder   =   null;
    }
    
    public List<Group> getGroupList() {
		return groupList;
	}

	public Groups setGroupList(List<Group> groupList) {
		logger.info("Creating the list of groups");
		this.groupList = groupList;
		for (Group grpBean : groupList){
			groupMap.put(grpBean.getName(), grpBean);
		}
		return this;
	}
	
    public void addGroup(Group group) {
    	this.groupList.add(group);
        this.groupMap.put(group.getName(), group);
    }
    
    public void setGroup(HashMap<String, Group> hm){
    	groupMap = hm;
    }
    public HashMap<String,Group> getGroups() {
        return this.groupMap;
    }

    public HashMap<String, Group> getGroupMap() {
		return groupMap;
	}

	public void setGroupMap(HashMap<String, Group> groupMap) {
		this.groupMap = groupMap;
	}

	public void setLogFolder(String value) {
        if(value != null && value.equals(""))
            throw new RuntimeException("Loader/Groups/LogFolder Can't be Empty!!!!");
        this.logFolder  =   value;
    }
    
    public String getLogFolder() {
        return this.logFolder;
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
 
    public String toString() {
        String str  = "Groups Log Folder:"+this.logFolder+"\n";
        logger.info("Groups "+this.groupMap);

        for(Group group : groupMap.values())
            str     += group.asString()+"\n";

        return str;
    }
}
