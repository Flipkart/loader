package com.flipkart.perf.server.resource;

import com.flipkart.perf.common.util.FileHelper;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.dropwizard.jersey.params.BooleanParam;
import com.yammer.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.flipkart.perf.server.cache.AgentsCache;
import com.flipkart.perf.server.config.AgentConfig;
import com.flipkart.perf.server.domain.LoaderAgent;
import com.flipkart.perf.server.exception.LibNotDeployedException;
import com.flipkart.perf.server.util.AgentHelper;
import com.flipkart.perf.server.util.DeploymentHelper;
import com.flipkart.perf.server.util.ResponseBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Resource to manage operations on loader-agents
 */
@Path("/agents")
public class AgentResource {
    private static Logger logger = LoggerFactory.getLogger(AgentResource.class);
    private AgentConfig agentConfig;

    public AgentResource(AgentConfig agentConfig){
        this.agentConfig = agentConfig;

        // Connect Will all agents which were not disabled earlier.
        for(LoaderAgent loaderAgent : AgentsCache.getAgentInfoMap().values()) {
            try {
                if(loaderAgent.getStatus() == LoaderAgent.LoaderAgentStatus.D_REGISTERED
                        || loaderAgent.getStatus() == LoaderAgent.LoaderAgentStatus.DISABLED)
                    continue;
                AgentHelper.refreshAgentInfo(loaderAgent);
            } catch (IOException e) {
                logger.error("Error While contacting Agent",e);
            }
        }
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Timed
    synchronized public LoaderAgent addAgent(@Context HttpServletRequest request,
                                             Map registrationParams) throws IOException, ExecutionException, InterruptedException {

        AgentsCache.addAgent(new LoaderAgent(request.getRemoteAddr(), registrationParams));
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

    @DELETE
    @Timed
    synchronized public void deRegisterAgent(@Context HttpServletRequest request) throws IOException, ExecutionException, InterruptedException {
        LoaderAgent agent = AgentsCache.getAgentInfo(request.getRemoteAddr());
        if(agent != null)
            agent.setDRegistered();

        throw new WebApplicationException(ResponseBuilder.agentNotRegistered(request.getRemoteAddr()));
    }

    @Path("/{agentIp}")
    @Produces(MediaType.APPLICATION_JSON)
    @DELETE
    @Timed
    synchronized public void deleteAgent(@PathParam("agentIp") String agentIp)
            throws IOException, ExecutionException, InterruptedException {
        if(AgentsCache.removeAgent(agentIp) == null)
            throw new WebApplicationException(ResponseBuilder.agentNotRegistered(agentIp));
    }

    @Path("/{agentIp}/tags")
    @Produces(MediaType.APPLICATION_JSON)
    @PUT
    @Timed
    synchronized public LoaderAgent addTags(@PathParam("agentIp") String agentIp, Set<String> tags)
            throws IOException, ExecutionException, InterruptedException {

        LoaderAgent agent = AgentsCache.getAgentInfo(agentIp);
        if(agent != null)
            return agent.setTags(tags);

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
        if(agent != null) {
            AgentHelper.refreshAgentInfo(agent);
            if(agent.getStatus() != LoaderAgent.LoaderAgentStatus.NOT_REACHABLE)
                agent.setEnabled();
            return agent;
        }

        throw new WebApplicationException(ResponseBuilder.agentNotRegistered(agentIp));
    }

    @Path("/{agentIPs}/libs/platformLibs")
    @POST
    @Timed
    synchronized public void deployPlatformLib(
            @PathParam("agentIPs") String agentIPs,
            @QueryParam("force") @DefaultValue("false")BooleanParam force) throws IOException, ExecutionException, InterruptedException, LibNotDeployedException {
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
            @QueryParam("force") @DefaultValue("false")BooleanParam force) throws IOException, ExecutionException, InterruptedException, LibNotDeployedException {
        String classes = FileHelper.readContent(classListInputStream);
        for(String agentIP : agentIPs.split(","))
            DeploymentHelper.instance().deployUDFLibsOnAgent(agentIP, classes, force.get());
    }
}