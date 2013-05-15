package com.open.perf.domain;

import com.open.perf.core.LoadController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Top Level Bean which is used to create Load configuration
 */
public class Load {
    private List<Group> groups;
    public Load() {
        this.groups = new ArrayList<Group>();
    }

    /**
     * Add group to your load work flow
     * @param group
     * @return
     */
    public Load addGroup(Group group) {
        this.groups.add(group);
        return this;
    }

    /**
     * Start Load
     * @throws Exception
     */
    public Load start(String jobId) throws Exception {
        // Validate if anything is wrong with the Load Configuration
        validate();
        resolveWarmUpGroups();

        // Start the Load Controller and Wait for Completion
        LoadController loadController =  new LoadController(jobId, this);
        loadController.start();
        loadController.join();
        return this;
    }

    /**
     * Validate Load Configuration
     */
    private void validate() {
        if(this.groups.size() == 0) {
            throw new RuntimeException("No Groups added to Load");
        }

        // Validate Each Group
        for(Group group : this.groups) {
            group.validate();
        }
    }

    /**
     * Create duplicate groups to simulate warm up functionality
     * @throws CloneNotSupportedException
     */
    private void resolveWarmUpGroups() throws CloneNotSupportedException {
        List<Group> warmUpGroups = new ArrayList<Group>();
        for(Group group : this.groups) {
            if(group.needsWarmUp()) {
                Group warmUpGroup = group.createWarmUpGroup();
                group.getDependOnGroups().add(0,warmUpGroup.getName());
                if(warmUpGroup.getGroupStartDelay() > 0)
                    group.setGroupStartDelay(0);

                warmUpGroups.add(warmUpGroup);
           }
        }

        for(Group warmUpGroup : warmUpGroups)
            this.groups.add(warmUpGroup);
    }

    public List<Group> getGroups() {
        return groups;
    }

    public Load setGroups(List<Group> groups) {
        this.groups = groups;
        return this;
    }

    public Map<String, Group> groupMap() {
        Map<String, Group>  groupMap = new HashMap<String, Group>();
        for(Group group : this.groups)
            groupMap.put(group.getName(), group);
        return groupMap;
    }

}
