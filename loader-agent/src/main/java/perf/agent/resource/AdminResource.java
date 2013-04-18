package perf.agent.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.agent.config.LoaderAgentConfiguration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/admin")

public class AdminResource {
    private static Logger logger = LoggerFactory.getLogger(AdminResource.class);
    private LoaderAgentConfiguration loaderAgentConfiguration;

    public AdminResource(LoaderAgentConfiguration loaderAgentConfiguration) {
        this.loaderAgentConfiguration = loaderAgentConfiguration;
    }
    /**
     * Get Agent Registration Information.
     * Mostly called by Loader-Server at its boot time to confirm the availability of agent
     * @return
     * @throws IOException
     */
    @Path("/registrationInfo")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map registrationInfo() throws IOException {
        return loaderAgentConfiguration.getRegistrationParams();
    }

}