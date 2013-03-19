package com.open.perf.domain;

import com.open.perf.core.LoadController;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Top Level Bean which is used to create Load configuration
 */
public class Loader {

    private String jobId;
	private String name;
    private Groups groups;
    private static Logger logger;

    static {
        logger              =   Logger.getLogger(Loader.class.getName());
    }

    public Loader() {
        this.groups = new Groups();
    }

    public Loader(String name) {
        this();
        this.name = name;
    }

    /**
     * Add group to your load work flow
     * @param group
     * @return
     */
    public Loader addGroup(Group group) {
        this.groups.addGroup(group);
        return this;
    }

    /**
     * Start Load
     * @throws Exception
     */
    public Loader start() throws Exception {
        // Validate if anything is wrong with the Loader Configuration
        validate();
        resolveWarmUpGroups();

        // Start the Load Controller and Wait for Completion
        LoadController loadController =  new LoadController(this.jobId, this.groups);
        loadController.start();
        loadController.join();
        logger.info("Logs in "+System.getProperty("BASE_PATH")+this.jobId);
        return this;
    }

    /**
     * Validate Loader Configuration
     */
    private void validate() {
        if(this.groups.getGroups().size() == 0) {
            throw new RuntimeException("No Groups added to Loader");
        }

        // Validate Each Group
        for(Group group : this.groups.getGroups()) {
            group.validate();
        }
    }

    /**
     * Create duplicate groups to simulate warm up functionality
     * @throws CloneNotSupportedException
     */
    private void resolveWarmUpGroups() throws CloneNotSupportedException {
        List<Group> warmUpGroups = new ArrayList<Group>();
        for(Group group : this.groups.getGroups()) {
            if(group.needsWarmUp()) {
                Group warmUpGroup = group.createWarmUpGroup();

                group.getDependOnGroups().add(0,warmUpGroup.getName());
                warmUpGroups.add(warmUpGroup);
           }
        }

        for(Group warmUpGroup : warmUpGroups)
            this.groups.addGroup(warmUpGroup);
    }
    
    

    public String getName() {
		return name;
	}

	public Loader setName(String name) {
		this.name = name;
		return this;
	}

	public Groups getGroups() {
        return groups;
    }

    public Loader setGroups(Groups groups) {
        this.groups = groups;
        return this;
    }

    public String getJobId() {
        return jobId;
    }

    public Loader setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }
}
