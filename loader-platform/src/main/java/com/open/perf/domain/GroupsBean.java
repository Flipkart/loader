package com.open.perf.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import com.open.perf.domain.GroupBean;


public class GroupsBean {
	
	@JsonProperty("groupList")
	private List<GroupBean> groupList;
	private HashMap<String,GroupBean> groupMap;
    private String logFolder;
    private static Logger logger;
    static {
        logger = Logger.getLogger(GroupsBean.class);
    }

    @JsonCreator
    public GroupsBean() {
    	logger.info("Created a blank groupsbean.");
        groupMap    =   new HashMap<String,GroupBean>();
        groupList = new ArrayList<GroupBean>();
        logFolder   =   null;
    }
    
    public List<GroupBean> getGroupList() {
		return groupList;
	}

	public GroupsBean setGroupList(List<GroupBean> groupList) {
		logger.info("Creating the list of groups");
		this.groupList = groupList;
		for (GroupBean grpBean : groupList){
			groupMap.put(grpBean.getName(), grpBean);
		}
		return this;
	}
	
    public void addGroup(GroupBean group) {
    	this.groupList.add(group);
        this.groupMap.put(group.getName(), group);
    }
    
    public void setGroup(HashMap<String, GroupBean> hm){
    	groupMap = hm;
    }
    public HashMap<String,GroupBean> getGroups() {
        return this.groupMap;
    }

    public HashMap<String, GroupBean> getGroupMap() {
		return groupMap;
	}

	public void setGroupMap(HashMap<String, GroupBean> groupMap) {
		this.groupMap = groupMap;
	}

	public void setLogFolder(String value) {
        if(value != null && value.equals(""))
            throw new RuntimeException("Loader/GroupsBean/LogFolder Can't be Empty!!!!");
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
 
    public String toString() {
        String str  = "GroupsBean Log Folder:"+this.logFolder+"\n";
        logger.info("GroupsBean "+this.groupMap);

        for(GroupBean group : groupMap.values())
            str     += group.asString()+"\n";

        return str;
    }
}
