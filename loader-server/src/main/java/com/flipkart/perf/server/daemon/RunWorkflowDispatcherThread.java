package com.flipkart.perf.server.daemon;

import com.flipkart.perf.server.domain.Job;
import com.flipkart.perf.server.domain.JobRequest;
import com.flipkart.perf.server.domain.RunWorkFlow;
import org.eclipse.jetty.util.BlockingArrayQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 08/10/13
 * Time: 2:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class RunWorkflowDispatcherThread implements Runnable {
    private static BlockingQueue<RunWorkFlow> workflowQueue;
    private static ArrayList<RunWorkFlow> runningWorkFlows;
    private static boolean keepRunning;
    private static final RunWorkflowDispatcherThread INSTANCE = new RunWorkflowDispatcherThread();

    @Override
    public void run() {
        while(keepRunning){
            while (!workflowQueue.isEmpty()){
                try {
                    RunWorkFlow workFlow = workflowQueue.take();
                    workFlow.setRunning(true);
                    workFlow.setStatus(RunWorkFlow.STATUS.RUNNING);
                    workFlow.persistWorkFlow();         //Do I need it here??
                    runningWorkFlows.add(workFlow);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException ie){
                    ie.printStackTrace();
                }
            }

            System.out.println("Added workflows to runningWorkFlows : " + runningWorkFlows.size());

            for(RunWorkFlow workFlow: runningWorkFlows){
                if(workFlow.getRunMap().isEmpty()){
                    workFlow.setRunning(false);
                    workFlow.setStatus(RunWorkFlow.STATUS.COMPLETE);
                    //workFlow.persistWorkFlow();         //move from in memory to disk
                    //runningWorkFlows.remove(workFlow); //remove from running queue
                } else {
                    if(allJobsComplete(workFlow.getRunningJobs())){
                        ArrayList<String> runs = workFlow.getRunMap().remove(0);
                        for(String run: runs){
                            JobRequest jobRequest = new JobRequest().setRunName(run);
                            try {
                                Job j = Job.raiseJobRequest(jobRequest);
                                workFlow.getRunningJobs().add(j);
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    }
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static void initialize(){
        workflowQueue = new BlockingArrayQueue<RunWorkFlow>();
        runningWorkFlows = new ArrayList<RunWorkFlow>();
        keepRunning = true;
    }

    private boolean allJobsComplete(ArrayList<Job> jobs){
        if(jobs==null) return true;
        for(Job job: jobs){
            if(job.isRunning() || job.isQueued()) return false;
        }
        return true;
    }

    public static RunWorkflowDispatcherThread getInstance(){
        return INSTANCE;
    }

    private RunWorkflowDispatcherThread(){

    }

    public void addRunWorkFlow(RunWorkFlow r){
        System.out.println("Adding workflow to queue");
        workflowQueue.add(r);
    }

    public static ArrayList<RunWorkFlow> getRunningWorkflows() {
        return runningWorkFlows;
    }


}
