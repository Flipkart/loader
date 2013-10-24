package com.flipkart.perf.server.daemon;

import com.flipkart.perf.server.domain.Job;
import com.flipkart.perf.server.domain.JobRequest;
import com.flipkart.perf.server.domain.ScheduledWorkFlowJob;
import org.eclipse.jetty.util.BlockingArrayQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 08/10/13
 * Time: 2:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScheduledWorkflowDispatcherThread implements Runnable {
    private static BlockingQueue<ScheduledWorkFlowJob> workflowQueue;
    private static ArrayList<ScheduledWorkFlowJob> runningWorkFlows;
    private static boolean keepRunning;
    private static final ScheduledWorkflowDispatcherThread INSTANCE = new ScheduledWorkflowDispatcherThread();

    @Override
    public void run() {
        while(keepRunning){
            while (!workflowQueue.isEmpty()){
                try {
                    ScheduledWorkFlowJob workFlow = workflowQueue.take();
                    workFlow.setRunning(true);
                    workFlow.setStatus(ScheduledWorkFlowJob.STATUS.RUNNING);
                    workFlow.persistWorkFlowJob();         //Do I need it here??
                    runningWorkFlows.add(workFlow);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException ie){
                    ie.printStackTrace();
                }
            }

            System.out.println("Added workflows to runningWorkFlows : " + runningWorkFlows.size());
            try {
                ArrayList<ScheduledWorkFlowJob> completedWorkflows = new ArrayList<ScheduledWorkFlowJob>();
                for(ScheduledWorkFlowJob workFlow: runningWorkFlows){
                    if(workFlow.getRunMap().isEmpty()){
                        if(allJobsComplete(workFlow.getRunningJobs())){
                            workFlow.setRunning(false);
                            workFlow.setStatus(ScheduledWorkFlowJob.STATUS.COMPLETE);
                            completedWorkflows.add(workFlow); //remove from running queue
                        }
                        workFlow.persistWorkFlowJob();         //move from in memory to disk
                    } else {
                        if(allJobsComplete(workFlow.getRunningJobs())){
                            ArrayList<String> runs = workFlow.getRunMap().remove(0);
                            System.out.println("Run Map size is " + workFlow.getRunMap().size());
                            for(String run: runs){
                                JobRequest jobRequest = new JobRequest().setRunName(run);
                                try {
                                    Job j = Job.raiseJobRequest(jobRequest);
                                    workFlow.getRunningJobs().add(j);
                                } catch (IOException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }
                            }
                            workFlow.persistWorkFlowJob();
                        }
                    }
                }
                runningWorkFlows.removeAll(completedWorkflows);
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static void initialize(){
        workflowQueue = new BlockingArrayQueue<ScheduledWorkFlowJob>();
        runningWorkFlows = new ArrayList<ScheduledWorkFlowJob>();
        keepRunning = true;
    }

    private boolean allJobsComplete(ArrayList<Job> jobs){
        if(jobs==null) return true;
        for(Job job: jobs){
            if(job.isRunning() || job.isQueued()) return false;
        }
        return true;
    }

    public static ScheduledWorkflowDispatcherThread getInstance(){
        return INSTANCE;
    }

    private ScheduledWorkflowDispatcherThread(){

    }

    public void addRunWorkFlow(ScheduledWorkFlowJob r){
        System.out.println("Adding workflow to queue");
        workflowQueue.add(r);
    }

    public static ArrayList<ScheduledWorkFlowJob> getRunningWorkflows() {
        return runningWorkFlows;
    }


}
