package com.flipkart.perf.server.resource;

import com.flipkart.perf.server.daemon.RunWorkflowDispatcherThread;
import com.flipkart.perf.server.domain.Job;
import com.flipkart.perf.server.domain.RunWorkFlow;
import com.flipkart.perf.server.domain.RunWorkFlowRequest;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 08/10/13
 * Time: 5:26 PM
 * To change this template use File | Settings | File Templates.
 */

@Path("/runWorkFlow")
public class RunWorkFlowResource {

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Timed
    public String submitWorkFlow(ArrayList<RunWorkFlowRequest> workflow){
        RunWorkFlow runWorkFlow = new RunWorkFlow(UUID.randomUUID().toString());
        runWorkFlow.setWorkflow(workflow);
        if(!runWorkFlow.checkCyclicDependency()){
            RunWorkflowDispatcherThread.getInstance().addRunWorkFlow(runWorkFlow);
        }
        return "{\"workflowId\":\"" + runWorkFlow.getWorkFlowId() + "\"}";
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    public ArrayList<RunWorkFlow> getWorkflows(@QueryParam("status") @DefaultValue("")String runningStatus) {
        if(runningStatus!=null && runningStatus.toLowerCase().equals("running"))
            return RunWorkflowDispatcherThread.getRunningWorkflows();

        return RunWorkFlow.searchRunWorkflows();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/{workflowId}")
    @Timed
    public RunWorkFlow getWorkflow(@PathParam("workflowId") String workflowId) {
        try {
            return RunWorkFlow.getRunworkFlow(workflowId);
        }   catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/{workflowId}/jobs")
    @Timed
    public ArrayList<Job> getWorkflowJobs(@PathParam("workflowId") String workflowId){
        try {
            return RunWorkFlow.getRunworkFlow(workflowId).getRunningJobs();
        } catch (Exception ie){
            ie.printStackTrace();
            return null;
        }
    }

}

