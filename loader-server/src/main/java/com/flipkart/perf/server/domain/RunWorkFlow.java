package com.flipkart.perf.server.domain;

import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.daemon.JobDispatcherThread;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.util.ArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 08/10/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class RunWorkFlow {

    private static final LoaderServerConfiguration configuration = LoaderServerConfiguration.instance();
    private static Logger logger = LoggerFactory.getLogger(RunWorkFlow.class);
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();
    public enum STATUS { RUNNING, QUEUED, COMPLETE, ERROR };

    @JsonIgnore
    private ArrayList<RunWorkFlowRequest> workflow;
    @JsonIgnore
    private ArrayList<ArrayList<String>> runMap;
    @JsonIgnore
    private boolean isRunning;

    private String workFlowId;
    private ArrayList<Job> runningJobs;
    private STATUS status;

    public RunWorkFlow(String workFlowId){
        this.workFlowId = workFlowId;
        runMap = new ArrayList<ArrayList<String>>();
        workflow = new ArrayList<RunWorkFlowRequest>();
        runningJobs = new ArrayList<Job>();
        isRunning = false;
        status = STATUS.QUEUED;
    }

    public RunWorkFlow(){}

    public ArrayList<RunWorkFlowRequest> getWorkflow() {
        return workflow;
    }

    public void setWorkflow(ArrayList<RunWorkFlowRequest> workflow) {
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

    public boolean checkCyclicDependency(){
        ArrayList<RunWorkFlowRequest> temp = workflow;
        System.out.println("temp size is : " + temp.size());
        while(!temp.isEmpty()){
            ArrayList<String> runRow = new ArrayList<String>();
            ArrayList<RunWorkFlowRequest> reqRow = new ArrayList<RunWorkFlowRequest>();
            for(RunWorkFlowRequest req: temp){
                if(req.getDependsOn().isEmpty()) {
                    runRow.add(req.getRunName());
                    reqRow.add(req);
                }
            }
            temp.removeAll(reqRow);
            System.out.println("temp size after removal : " + temp.size());
            for(String runName: runRow){
                for(RunWorkFlowRequest req: temp){
                    if(req.getDependsOn().contains(runName)) req.getDependsOn().remove(runName);
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

    public void persistWorkFlow() throws IOException {
        System.out.println("Status file Path : " + configuration.getJobFSConfig().getWorkflowStatusPath(this.workFlowId));
        File workFlowStatusFile = new File(configuration.getJobFSConfig().getWorkflowStatusPath(this.workFlowId));
        if(!workFlowStatusFile.exists())
            FileHelper.createFilePath(workFlowStatusFile.getAbsolutePath());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(workFlowStatusFile), this);
    }

    public static ArrayList<RunWorkFlow> searchRunWorkflows() {
        String workflowsPath = LoaderServerConfiguration.instance().getJobFSConfig().getWorkflowsPath();
        File[] workflowDirs = new File(workflowsPath).listFiles();
        ArrayList<RunWorkFlow> result = new ArrayList<RunWorkFlow>();
        for(int i=0;i<workflowDirs.length;i++) {
            if(workflowDirs[i].isDirectory()){
                try {
                    result.add(objectMapper.readValue(new FileInputStream(workflowDirs[i].getAbsolutePath() + "/status.json"), RunWorkFlow.class));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static RunWorkFlow getRunworkFlow(String workFlowId) throws IOException {
        File statusFile = new File(LoaderServerConfiguration.instance().getJobFSConfig().getWorkflowStatusPath(workFlowId));
        if(statusFile.exists()){
            return objectMapper.readValue(new FileInputStream(statusFile), RunWorkFlow.class);
        }
        return null;
    }

}
