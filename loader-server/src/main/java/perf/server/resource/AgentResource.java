package perf.server.resource;

import com.open.perf.util.FileHelper;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.dropwizard.jersey.params.BooleanParam;
import com.yammer.metrics.annotation.Timed;
import org.apache.log4j.Logger;
import perf.server.cache.AgentsCache;
import perf.server.domain.LoaderAgent;
import perf.server.util.DeploymentHelper;
import perf.server.util.ResponseBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Resource to manage operations on loader-agents
 */
@Path("/agents")
public class AgentResource {
    private static Logger log = Logger.getLogger(AgentResource.class);

    public AgentResource(){}

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Timed
    synchronized public LoaderAgent addAgent(@Context HttpServletRequest request,
                                             Map registrationParams) throws IOException, ExecutionException, InterruptedException {

        AgentsCache.addAgentInfoMap(new LoaderAgent(request.getRemoteAddr(), registrationParams));
        return AgentsCache.getAgentInfo(request.getRemoteAddr());
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    synchronized public Map<String, LoaderAgent> getAgents()
            throws IOException, ExecutionException, InterruptedException {
        return AgentsCache.getAgentInfoMap();
    }

    @Path("/{agentIp}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Timed
    synchronized public LoaderAgent getAgent(@PathParam("agentIp") String agentIp)
            throws IOException, ExecutionException, InterruptedException {

        LoaderAgent agent = AgentsCache.getAgentInfo(agentIp);
        if(agent != null)
            return agent;

        throw new WebApplicationException(ResponseBuilder.agentNotRegistered(agentIp));
    }

    @Path("/{agentIp}")
    @Produces(MediaType.APPLICATION_JSON)
    @DELETE
    @Timed
    synchronized public LoaderAgent deleteAgent(@PathParam("agentIp") String agentIp)
            throws IOException, ExecutionException, InterruptedException {

        LoaderAgent agent = AgentsCache.removeAgentInfo(agentIp);
        if(agent != null)
            return agent;

        throw new WebApplicationException(ResponseBuilder.agentNotRegistered(agentIp));
    }

    @Path("/{agentIp}/disable")
    @Produces(MediaType.APPLICATION_JSON)
    @PUT
    @Timed
    synchronized public LoaderAgent disableAgent(@PathParam("agentIp") String agentIp)
            throws IOException, ExecutionException, InterruptedException {

        LoaderAgent agent = AgentsCache.getAgentInfo(agentIp);
        if(agent != null)
            return agent.setDisabled();

        throw new WebApplicationException(ResponseBuilder.agentNotRegistered(agentIp));
    }

    @Path("/{agentIp}/enable")
    @Produces(MediaType.APPLICATION_JSON)
    @PUT
    @Timed
    synchronized public LoaderAgent enableAgent(@PathParam("agentIp") String agentIp)
            throws IOException, ExecutionException, InterruptedException {

        LoaderAgent agent = AgentsCache.getAgentInfo(agentIp);
        if(agent != null)
            return agent.setEnabled();

        throw new WebApplicationException(ResponseBuilder.agentNotRegistered(agentIp));
    }



    @Path("/{agentIPs}/libs/platformLibs")
    @POST
    @Timed
    synchronized public void deployPlatformLib(
            @PathParam("agentIPs") String agentIPs,
            @QueryParam("force") @DefaultValue("false")BooleanParam force) throws IOException, ExecutionException, InterruptedException {
        for(String agentIP : agentIPs.split(","))
            DeploymentHelper.instance().deployPlatformLibsOnAgent(agentIP, force.get());
    }



/*
    Following call simulates html form post call
    curl
       -X POST
       -H "Content-Type: multipart/form-data"
       -F "classList=@Path-To-File-Containing-Class-Names-Separated-With-New-Line"
       http://localhost:9999/loader-server/agents/{comma separated ips}/libs/classLibs
*/

    @Path("/{agentIPs}/libs/classLibs")
    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    synchronized public void deployClassLib(
            @FormDataParam("classList") InputStream classListInputStream,
            @PathParam("agentIPs") String agentIPs,
            @QueryParam("force") @DefaultValue("false")BooleanParam force) throws IOException, ExecutionException, InterruptedException {
        String classes = FileHelper.readContent(classListInputStream);
        for(String agentIP : agentIPs.split(","))
            DeploymentHelper.instance().deployClassLibsOnAgent(agentIP, classes, force.get());
    }
}