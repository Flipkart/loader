package com.flipkart.perf.server.domain;

import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.daemon.ScheduledWorkflowDispatcherThread;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 11/10/13
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
@DisallowConcurrentExecution
public class ScheduledWorkflowJobCreator implements org.quartz.Job {

    private static Logger logger = LoggerFactory.getLogger(ScheduledWorkflowJobCreator.class);
    private String workflowName;

    public ScheduledWorkflowJobCreator(){

    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //To change body of implemented methods use File | Settings | File Templates.
        String workflowId = UUID.randomUUID().toString();
        ObjectMapper mapper = ObjectMapperUtil.instance();
        ScheduledWorkFlowJob scheduledWorkFlowJob = new ScheduledWorkFlowJob(workflowId, workflowName);
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        ArrayList<ScheduledWorkflowEntities> workflow = null;
        try {
            ArrayList<HashMap<String,String>> tempWorkflow = mapper.readValue(jobDataMap.get("workflow").toString(), ArrayList.class);
            workflow = new ArrayList<ScheduledWorkflowEntities>();
            for(HashMap<String,String>hm : tempWorkflow){
                workflow.add(mapper.convertValue(hm, ScheduledWorkflowEntities.class));
            }
            scheduledWorkFlowJob.setWorkflow(workflow);
            if(scheduledWorkFlowJob.checkCyclicDependency()) throw new RuntimeException("Cyclic Dependency found");

            String runningFilePath = LoaderServerConfiguration.
                    instance().
                    getJobFSConfig().
                    getScheduledWorkflowRunningFile(workflowName);
            String workflowJobPath = LoaderServerConfiguration.
                    instance().
                    getJobFSConfig().
                    getScheduledWorkflowJobsFile(workflowName);
            File runningFile = new File(runningFilePath);
            File jobsFile = new File(workflowJobPath);
            ArrayList<String> runningWorkFlows = new ArrayList<String>();
            ArrayList<String> allWorkflows = new ArrayList<String>();
            if(!runningFile.exists()){
                FileHelper.createFile(runningFilePath);
                mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(runningFile), runningWorkFlows);
            }
            if(!jobsFile.exists()){
                FileHelper.createFile(workflowJobPath);
                mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(jobsFile), allWorkflows);
            }
            runningWorkFlows = mapper.readValue(runningFile, ArrayList.class);
            ScheduledWorkflowDispatcherThread.getInstance().addRunWorkFlow(scheduledWorkFlowJob);
            runningWorkFlows.add(workflowId);
            allWorkflows = mapper.readValue(jobsFile, ArrayList.class);
            allWorkflows.add(workflowId);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(runningFile),runningWorkFlows);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(jobsFile), allWorkflows);
            while(!scheduledWorkFlowJob.isComplete()){
                logger.info("workflow still running going to sleep");
                Thread.sleep(5000);
            }
            runningWorkFlows = mapper.readValue(runningFile, ArrayList.class);
            runningWorkFlows.remove(workflowId);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(runningFile), runningWorkFlows);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}