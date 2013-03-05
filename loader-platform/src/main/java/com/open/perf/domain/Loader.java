package com.open.perf.domain;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import com.open.perf.util.HelperUtil;
import com.open.perf.load.LoadController;


public class Loader {

	@JsonProperty
	private String name;
	@JsonProperty
    private Groups groups;
    private String logFolder;
    private static Logger logger;

    static {
        logger              =   Logger.getLogger(Loader.class.getName());
    }

    @JsonCreator
    public Loader(@JsonProperty("name")String name) {
        this.name = name;
        this.groups = new Groups();
        this.logFolder = "/var/log/loader/"+ name.replace(" ","");
        this.groups.setLogFolder(this.logFolder+"/groups");
        logger.info("Created a blank loader instance");
    }

    public Loader addGroup(Group group) {
        this.groups.addGroup(group);
        if(group.getLogFolder() == null || group.getLogFolder().equals(""))
            group.setLogFolder(this.logFolder+"/groups/"+ group.getName().replace(" ",""));

        for(GroupFunction groupFunction : group.getFunctions()) {
            groupFunction.setStatFile(group.getLogFolder() + "/" + groupFunction.getName() + "_" + groupFunction.getClassName() + "." + groupFunction.getFunctionName() + ".txt");
//            groupFunction.setPercentileStatFile(group.getLogFolder() + "/" + groupFunction.getName() + "_" + groupFunction.getClassName() + "." + groupFunction.getFunctionName() + "_percentiles.txt");
        }
        return this;
    }

    public void start() throws Exception {
        logger.info("Use ON_SCREEN_STATS env variable to see stats on console");
        archiveOld();
        logger.info("Archieved the old log forlders");
        resolveWarmUpGroups();
        logger.info("Resolved warmup groups");
        LoadController loadController             =   null;

        if(this.groups.getGroups().size() > 0) {
            try {
                loadController             =   new LoadController(groups);
            } catch (Exception e) {
                logger.error("Load Controller Failed with:\n"+HelperUtil.getExceptionString(e));
            }
        }

        if(loadController !=  null) {
        	logger.info("Starting Loader\n");
            loadController.start();
            loadController.join();
        }
    }

    private void resolveWarmUpGroups() throws CloneNotSupportedException {
        List<Group> warmUpGroups = new ArrayList<Group>();
        for(Group group : this.groups.getGroups().values()) {
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
    	logger.info("Setting up groups and logfolder for groups");
        this.groups = groups;
        this.groups.setLogFolder(this.logFolder+"/groups");
        return this;
    }

    private void archiveOld() {
        File oldDir = new File(this.logFolder);
        if(oldDir.exists()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
            String date = simpleDateFormat.format(new Date());
            new File(this.logFolder).renameTo(new File(this.logFolder+"_"+date));
        }
    }
    
    public void setLogFolder(String logFolder){
    	this.logFolder = logFolder;
    }
    
    public String getLogFolder(){
    	return logFolder;
    }

    public String toString() {
        String info = "Load : "+this.name;
        info += "Has Following Groups \n:"+this.groups.toString();
        return info;
    }

}
