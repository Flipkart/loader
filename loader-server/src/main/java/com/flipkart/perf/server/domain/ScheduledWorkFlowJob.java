package com.flipkart.perf.server.domain;

import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 08/10/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScheduledWorkFlowJob {

    private static final LoaderServerConfiguration configuration = LoaderServerConfiguration.instance();
    private static Logger logger = LoggerFactory.getLogger(ScheduledWorkFlowJob.class);
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();
    public enum STATUS { RUNNING, QUEUED, COMPLETE, ERROR };

    @JsonIgnore
    private ArrayList<ScheduledWorkflowEntities> workflow;
    @JsonIgnore
    private ArrayList<ArrayList<String>> runMap;
    @JsonIgnore
    private boolean isRunning;

    private String workFlowId;
    private ArrayList<Job> runningJobs;
    private STATUS status;
    private String workflowName;

    public ScheduledWorkFlowJob(String workFlowId, String workflowName){
        this.workFlowId = workFlowId;
        runMap = new ArrayList<ArrayList<String>>();
        workflow = new ArrayList<ScheduledWorkflowEntities>();
        runningJobs = new ArrayList<Job>();
        isRunning = false;
        status = STATUS.QUEUED;
        this.workflowName = workflowName;
    }

    public ScheduledWorkFlowJob(){}

    public ArrayList<ScheduledWorkflowEntities> getWorkflow() {
        return workflow;
    }

    public void setWorkflow(ArrayList<ScheduledWorkflowEntities> workflow) {
        this.workflow = workflow;
    }

    public ArrayList<ArrayList<String>> getRunMap() {
        return runMap;
    }

    public void setRunMap(ArrayList<ArrayList<String>> runMap) {
        this.runMap = runMap;
    }

    public String getWorkFlowId() {
        return workFlowId;
    }

    public void setWorkFlowId(String workFlowId) {
        this.workFlowId = workFlowId;
    }

    public ArrayList<Job> getRunningJobs() {
        return runningJobs;
    }

    public void setRunningJobs(ArrayList<Job> runningJobsId) {
        this.runningJobs = runningJobsId;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public boolean checkCyclicDependency(){
        ArrayList<ScheduledWorkflowEntities> temp = workflow;    //I don't need workFlow so destroying it while checking
        System.out.println("temp size is : " + temp.size());
        while(!temp.isEmpty()){
            ArrayList<String> runRow = new ArrayList<String>();
            ArrayList<String> blockRow = new ArrayList<String>();
            ArrayList<ScheduledWorkflowEntities> reqRow = new ArrayList<ScheduledWorkflowEntities>();
            for(ScheduledWorkflowEntities req: temp){
                if(req.getDependsOn().isEmpty()) {
                    runRow.add(req.getRunName());
                    blockRow.add(req.getBlockName());
                    reqRow.add(req);
                }
            }
            temp.removeAll(reqRow);
            System.out.println("temp size after removal : " + temp.size());
            for(String blockName: blockRow){
                for(ScheduledWorkflowEntities req: temp){
                    if(req.getDependsOn().contains(blockName)) req.getDependsOn().remove(blockName);
                }
            }
            if(runRow.isEmpty()) return true;
            runMap.add(runRow);
        }
        System.out.println("Dependency Map is");
        for(ArrayList<String> wf: runMap){
            for(String runName: wf){
                System.out.print(runName + " ->");
            }
            System.out.println();
        }
        return false;
    }

    public void persistWorkFlowJob() throws IOException {
        System.out.println("Status file Path : " + configuration.getJobFSConfig().getWorkflowJobStatusPath(this.workFlowId));
        File workFlowStatusFile = new File(configuration.getJobFSConfig().getWorkflowJobStatusPath(this.workFlowId));
        if(!workFlowStatusFile.exists())
            FileHelper.createFilePath(workFlowStatusFile.getAbsolutePath());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(workFlowStatusFile), this);
    }

    public static ScheduledWorkFlowJob getRunWorkFlowJob(String workFlowId) throws IOException {
        File statusFile = new File(LoaderServerConfiguration.instance().getJobFSConfig().getWorkflowJobStatusPath(workFlowId));
        if(statusFile.exists()){
            return objectMapper.readValue(new FileInputStream(statusFile), ScheduledWorkFlowJob.class);
        }
        return null;
    }

    @JsonIgnore
    public boolean isComplete(){
        if(status.equals(STATUS.QUEUED) || status.equals(STATUS.RUNNING)) return false;
        return  true;
    }

    public static List<ScheduledWorkFlowJob> searchWorkflowJobs(String pageSize, String pageNumber, String workflowJobId,
                                                     String workflowName, String status) throws IOException {

        String workflowsPath = LoaderServerConfiguration.instance().getJobFSConfig().getWorkflowJobsPath();
        File[] workflowDirs = new File(workflowsPath).listFiles();
        Arrays.sort(workflowDirs, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        ArrayList<ScheduledWorkFlowJob> result = new ArrayList<ScheduledWorkFlowJob>();
        for(File workflowDir: workflowDirs){
            File f = new File(workflowDir.getAbsolutePath() + File.separator + "status.json");
            ScheduledWorkFlowJob scheduledWorkFlowJob = objectMapper.readValue(f, ScheduledWorkFlowJob.class);
            result.add(scheduledWorkFlowJob);
        }
        ArrayList<ScheduledWorkFlowJob> filter = new ArrayList<ScheduledWorkFlowJob>();
        if(!workflowJobId.equals("")){
            for(ScheduledWorkFlowJob r:result){
                if(r.getWorkFlowId().contains(workflowJobId)) filter.add(r);
            }
        }
        result.removeAll(filter);
        filter.clear();
        if(!workflowName.equals("")) {
            for(ScheduledWorkFlowJob r:result){
                if(r.getWorkflowName().contains(workflowName)) filter.add(r);
            }
        }
        result.removeAll(filter);
        filter.clear();
        if(!status.equals("")){
            for(ScheduledWorkFlowJob r:result){
                if(r.getStatus().equals(status)) filter.add(r);
            }
        }
        result.removeAll(filter);
        filter.clear();
        int size = Integer.parseInt(pageSize);
        int numberOfPages = result.size()/size + 1;
        int startIndex = (Integer.parseInt(pageNumber)-1) * size;
        int lastIndex = startIndex + size > result.size()?result.size():startIndex+size;
        return result.subList(startIndex,lastIndex);
    }

    public static void deleteWorkflowJob(String workflowJobId) throws IOException {
        String workflowJobPath = LoaderServerConfiguration.instance().getJobFSConfig().getWorkflowJobPath(workflowJobId);
        File file = new File(workflowJobPath);
        if(!file.exists()) throw new RuntimeException("WorkflowJobId " + workflowJobId + " not found.");
        File statusFile = new File(LoaderServerConfiguration.instance().getJobFSConfig().getWorkflowJobStatusPath(workflowJobId));
        ScheduledWorkFlowJob workflow = objectMapper.readValue(statusFile, ScheduledWorkFlowJob.class);
        FileHelper.deleteRecursively(file);
        File workflowJobsFile = new File(LoaderServerConfiguration.instance().getJobFSConfig().
                getScheduledWorkflowJobsFile(workflow.getWorkflowName()));
        ArrayList<String> allWorkflowJobIds = objectMapper.readValue(workflowJobsFile, ArrayList.class);
        allWorkflowJobIds.remove(workflowJobId);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(workflowJobsFile,allWorkflowJobIds);
    }

}
