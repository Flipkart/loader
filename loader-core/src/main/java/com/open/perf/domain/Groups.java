package com.open.perf.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Groups {
    private List<Group> groups;
	private HashMap<String,Group> groupMap;

    public Groups() {
        groupMap    =   new HashMap<String,Group>();
        groups = new ArrayList<Group>();
    }
    
    public List<Group> getGroups() {
		return groups;
	}

	public Groups setGroups(List<Group> groups) {
		this.groups = groups;
		for (Group grpBean : groups){
			groupMap.put(grpBean.getName(), grpBean);
		}
		return this;
	}
	
    public void addGroup(Group group) {
    	this.groups.add(group);
        this.groupMap.put(group.getName(), group);
    }
    
    public HashMap<String,Group> getGroupMap() {
        return this.groupMap;
    }
}
