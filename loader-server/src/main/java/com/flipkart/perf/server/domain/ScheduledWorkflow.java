package com.flipkart.perf.server.domain;

import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 10/10/13
 * Time: 5:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScheduledWorkflow {
    private ArrayList<ScheduledWorkflowEntities> workflow;
    private boolean startNow;
    private boolean runForEver;
    private String startAt;
    private String endAt;
    private int repeats;
    private int interval;
    private String schedulerType;
    private String cronExpression;
    private String name;

    public ArrayList<ScheduledWorkflowEntities> getWorkflow() {
        return workflow;
    }

    public void setWorkflow(ArrayList<ScheduledWorkflowEntities> workflow) {
        this.workflow = workflow;
    }

    public boolean isStartNow() {
        return startNow;
    }

    public void setStartNow(boolean startNow) {
        this.startNow = startNow;
    }

    public boolean isRunForEver() {
        return runForEver;
    }

    public void setRunForEver(boolean runForEver) {
        this.runForEver = runForEver;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public int getRepeats() {
        return repeats;
    }

    public void setRepeats(int repeats) {
        this.repeats = repeats;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getSchedulerType() {
        return schedulerType;
    }

    public void setSchedulerType(String schedulerType) {
        this.schedulerType = schedulerType;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScheduledWorkflow persist() throws IOException {
        String path = LoaderServerConfiguration.instance().getJobFSConfig().getScheduledWorkflowPath(this.name);
        File scheduledWorkflowFile = new File(path + File.separator + "workflow.json");
        if(!scheduledWorkflowFile.exists())
            FileHelper.createFilePath(scheduledWorkflowFile.getAbsolutePath());
        ObjectMapper mapper = ObjectMapperUtil.instance();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(scheduledWorkflowFile), this);
        return this;
    }

    public static ScheduledWorkflow getScheduledWorkflow(String workflowName) throws IOException {
        String path = LoaderServerConfiguration.instance().getJobFSConfig().getScheduledWorkflowPath(workflowName);
        File scheduledWorkflowFile = new File(path + "/workflow.json");
        if(!scheduledWorkflowFile.exists()) throw new RuntimeException("Workflow " + workflowName + " doesn't exists!");
        ObjectMapper mapper = ObjectMapperUtil.instance();
        ScheduledWorkflow scheduledWorkflow = mapper.readValue(scheduledWorkflowFile, ScheduledWorkflow.class);
        return scheduledWorkflow;
    }


    public static void deleteWorkflow(String workflowName) {
        String path = LoaderServerConfiguration.instance().getJobFSConfig().getScheduledWorkflowPath(workflowName);
        File scheduledWorkflowFile = new File(path);
        if(!scheduledWorkflowFile.exists()) throw new RuntimeException("Workflow " + workflowName + " doesn't exists!");
        FileHelper.deleteRecursively(scheduledWorkflowFile);
    }

    public static ArrayList<String> getAllJobs(String workflowName) throws IOException {
        String path = LoaderServerConfiguration.instance().getJobFSConfig().getScheduledWorkflowJobsFile(workflowName);
        File allJobsFile = new File(path);
        ArrayList<String> allJobs = new ArrayList<String>();
        ObjectMapper mapper = ObjectMapperUtil.instance();
        allJobs = mapper.readValue(allJobsFile, ArrayList.class);
        return  allJobs;
    }

    public static boolean workflowExists(String workflowName) {
        File f = new File(LoaderServerConfiguration.instance().getJobFSConfig().getScheduledWorkflowPath(workflowName));
        return f.exists();
    }


    public static ArrayList<ScheduledWorkflow> searchScheduledWorkflows(String workflowName, String runName) {
        String workflowsPath = LoaderServerConfiguration.instance().getJobFSConfig().getScheduledWorkflowsPath();
        File[] workflowDirs = new File(workflowsPath).listFiles();
        ObjectMapper objectMapper = ObjectMapperUtil.instance();
        Arrays.sort(workflowDirs, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        ArrayList<ScheduledWorkflow> result = new ArrayList<ScheduledWorkflow>();
        for(int i=0;i<workflowDirs.length;i++) {
            if(workflowDirs[i].isDirectory()){
                try {
                    result.add(objectMapper.readValue(new FileInputStream(workflowDirs[i].getAbsolutePath()
                            + File.separator + "workflow.json"), ScheduledWorkflow.class));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        ArrayList<ScheduledWorkflow> filter = new ArrayList<ScheduledWorkflow>();
        if(workflowName!=null && !workflowName.equals("")){
            for(ScheduledWorkflow scheduledWorklow :result){
                if(!scheduledWorklow.getName().toLowerCase().contains(workflowName.toLowerCase()))
                    filter.add(scheduledWorklow);
            }
            result.removeAll(filter);
            filter.clear();
        }
        if(runName!=null && !runName.equals("")){
            for(ScheduledWorkflow scheduledWorkflow :result){
                ArrayList<ScheduledWorkflowEntities> blocks = scheduledWorkflow.getWorkflow();
                boolean found = false;
                for(ScheduledWorkflowEntities block: blocks){
                    if(block.getRunName().toLowerCase().contains(runName.toLowerCase())){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    filter.add(scheduledWorkflow);
                }
            }
            result.removeAll(filter);
            filter.clear();
        }
        return result;
    }

}
