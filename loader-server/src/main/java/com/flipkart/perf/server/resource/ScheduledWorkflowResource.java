package com.flipkart.perf.server.resource;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 10/10/13
 * Time: 4:03 PM
 * To change this template use File | Settings | File Templates.
 */

import com.flipkart.perf.server.domain.ScheduledWorkflow;
import com.flipkart.perf.server.domain.WorkflowScheduler;
import com.yammer.metrics.annotation.Timed;
import org.quartz.SchedulerException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

@Path("/scheduledWorkFlows")
public class ScheduledWorkflowResource {

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Timed
    public void createScheduledWorkFlow(ScheduledWorkflow workflow) throws SchedulerException, IOException {
        if(ScheduledWorkflow.workflowExists(workflow.getName())) throw new RuntimeException("Run already exists");
        workflow.persist();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    public ArrayList<ScheduledWorkflow> getAllScheduledWorkFlows(@QueryParam("workflowName")@DefaultValue("") String workflowName,
                                                      @QueryParam("runName")@DefaultValue("")String runName) {
        return ScheduledWorkflow.searchScheduledWorkflows(workflowName,runName);
    }



    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{scheduledWorkflowName}")
    @GET
    @Timed
    public ScheduledWorkflow getScheduledWorkflow(@PathParam("scheduledWorkflowName") String workflowName) throws IOException {
        return ScheduledWorkflow.getScheduledWorkflow(workflowName);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{scheduledWorkflowName}")
    @PUT
    @Timed
    public ScheduledWorkflow updateWorkflow(@PathParam("scheduledWorkflowName") String workflowName, ScheduledWorkflow workflow) throws IOException {
        if(!workflowName.equals(workflow.getName())) throw new RuntimeException("Resource and Body validation failed!");
        return workflow.persist();
    }

    @Path("/{scheduledWorkflowName}")
    @DELETE
    @Timed
    public void deleteScheduledWorkflow(@PathParam("scheduledWorkflowName") String workflowName ){
        ScheduledWorkflow.deleteWorkflow(workflowName);
    }

    @Path("/{scheduledWorkflowName}/execute")
    @POST
    @Timed
    public String executeScheduledWorkflow(@PathParam("scheduledWorkflowName")String workflowName) throws IOException, SchedulerException, ParseException {
        ScheduledWorkflow workflow = ScheduledWorkflow.getScheduledWorkflow(workflowName);
        return WorkflowScheduler.createSchedule(workflow);
    }

    @Path("/{scheduledWorkflowName}/kill")
    @PUT
    @Timed
    public void killScheduledWorkflow(@PathParam("scheduledWorkflowName") String workflowName) throws SchedulerException {
        WorkflowScheduler.removeWorkflow(workflowName);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{scheduledWorkflowName}/workflowJobs")
    @GET
    @Timed
    public ArrayList<String> getAllJobsForWorkflow(@PathParam("scheduledWorkflowName") String workflowName) throws IOException {
        return ScheduledWorkflow.getAllJobs(workflowName);
    }


}
