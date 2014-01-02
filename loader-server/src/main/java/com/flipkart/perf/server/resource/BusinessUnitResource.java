package com.flipkart.perf.server.resource;


import com.yammer.dropwizard.jersey.params.BooleanParam;
import com.yammer.metrics.annotation.Timed;
import org.codehaus.jackson.map.ObjectMapper;
import com.flipkart.perf.server.config.JobFSConfig;
import com.flipkart.perf.server.domain.BusinessUnit;
import com.flipkart.perf.server.domain.PerformanceRun;
import com.flipkart.perf.server.domain.Team;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import com.flipkart.perf.server.util.ResponseBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * businessUnits/scp/teams/oms/runs/omsSingleThreadRun
 */
@Path("/businessUnits")
public class BusinessUnitResource {

    private final JobFSConfig jobFSConfig;
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();

    public BusinessUnitResource(JobFSConfig jobFSConfig) {
        this.jobFSConfig = jobFSConfig;
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    public Map<String, BusinessUnit> getBusinessUnits() throws IOException {
        return BusinessUnit.all();
    }

    /**
     * Http Post with Body
     * {
            "name" : "BU1",
            "teams" : [ {
                "name" : "Team1"
     } ]
     }
     * @param businessUnit
     * @return
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Timed
    public Response createBusinessUnit(BusinessUnit businessUnit) throws IOException {
        if(businessUnit.exists())
            throw new WebApplicationException(ResponseBuilder.resourceAlreadyExists("Business Unit",businessUnit.getName()));
        businessUnit.persist();
        return ResponseBuilder.resourceCreated("BusinessUnit", businessUnit.getName());
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{businessUnit}")
    @GET
    @Timed
    public BusinessUnit getBusinessUnit(@PathParam("businessUnit") String businessUnitName) throws IOException {
        return BusinessUnit.businessUnitExistsOrException(businessUnitName);
    }

    @Path("/{businessUnit}")
    @DELETE
    @Timed
    public Response deleteBusinessUnit(@PathParam("businessUnit") String businessUnitName) throws IOException {
        BusinessUnit businessUnit = BusinessUnit.businessUnitExistsOrException(businessUnitName);
        businessUnit.delete();
        return ResponseBuilder.resourceDeleted("BusinessUnit", businessUnit.getName());
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{businessUnit}/teams")
    @POST
    @Timed
    public BusinessUnit addTeam(@PathParam("businessUnit") String businessUnitName, Team team) throws IOException {
        BusinessUnit businessUnit = BusinessUnit.businessUnitExistsOrException(businessUnitName);
        if(businessUnit.teamExist(team.getName()))
            throw new WebApplicationException(ResponseBuilder.resourceAlreadyExists("Team",team.getName()));
        return businessUnit.addTeam(team);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{businessUnit}/teams")
    @GET
    @Timed
    public Map<String, Team> getTeams(@PathParam("businessUnit") String businessUnitName) throws IOException {
        BusinessUnit businessUnit = BusinessUnit.businessUnitExistsOrException(businessUnitName);
        return businessUnit.getTeams();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{businessUnit}/teams/{team}")
    @GET
    @Timed
    public Team getTeam(@PathParam("businessUnit") String businessUnitName, @PathParam("team") String teamName) throws IOException {
        BusinessUnit businessUnit = BusinessUnit.businessUnitExistsOrException(businessUnitName);
        if(!businessUnit.teamExist(teamName))
            throw new WebApplicationException(ResponseBuilder.resourceNotFound("Team",teamName));
        return businessUnit.getTeam(teamName);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{businessUnit}/teams/{team}")
    @DELETE
    @Timed
    public Response deleteTeam(@PathParam("businessUnit") String businessUnitName, @PathParam("team") String teamName) throws IOException {
        BusinessUnit businessUnit = BusinessUnit.businessUnitExistsOrException(businessUnitName);
        if(!businessUnit.teamExist(teamName))
            throw new WebApplicationException(ResponseBuilder.resourceNotFound("Team", teamName));
        businessUnit.deleteTeam(teamName);
        return ResponseBuilder.resourceDeleted("Team", teamName);
    }

    /**
     *
     * @param businessUnitName
     * @param teamName
     * @param runs ["run1", "run2"]
     * @return
     * @throws IOException
     */
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{businessUnit}/teams/{team}/runs")
    @POST
    @Timed
    public BusinessUnit addRuns(@PathParam("businessUnit") String businessUnitName, @PathParam("team") String teamName, List<String> runs) throws IOException {
        BusinessUnit businessUnit = BusinessUnit.businessUnitExistsOrException(businessUnitName);
        if(!businessUnit.teamExist(teamName))
            throw new WebApplicationException(ResponseBuilder.resourceNotFound("Team",teamName));
        return businessUnit.addRuns(teamName, runs);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{businessUnit}/teams/{team}/runs")
    @GET
    @Timed
    public List<String> getRuns(@PathParam("businessUnit") String businessUnitName, @PathParam("team") String teamName) throws IOException {
        BusinessUnit businessUnit = BusinessUnit.businessUnitExistsOrException(businessUnitName);
        if(!businessUnit.teamExist(teamName))
            throw new WebApplicationException(ResponseBuilder.resourceNotFound("Team",teamName));
        return businessUnit.getTeam(teamName).getRuns();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{businessUnit}/teams/{team}/runs/{run}")
    @GET
    @Timed
    public PerformanceRun getRun(@PathParam("businessUnit") String businessUnitName, @PathParam("team") String teamName, @PathParam("run") String run) throws IOException {
        BusinessUnit businessUnit = BusinessUnit.businessUnitExistsOrException(businessUnitName);
        businessUnit.teamExistOrException(teamName).runExistsOrException(run);
        return PerformanceRun.runExistsOrException(run);
    }

    @Path("/{businessUnit}/teams/{team}/runs/{run}")
    @DELETE
    @Timed
    public void deleteRun(@PathParam("businessUnit") String businessUnitName,
                          @PathParam("team") String teamName,
                          @PathParam("run") String run,
                          @QueryParam("deleteJobs") @DefaultValue("false")BooleanParam deleteJobs) throws IOException {
        BusinessUnit businessUnit = BusinessUnit.businessUnitExistsOrException(businessUnitName);
        businessUnit.teamExistOrException(teamName).runExistsOrException(run);
        PerformanceRun.runExistsOrException(run).delete();
    }

}