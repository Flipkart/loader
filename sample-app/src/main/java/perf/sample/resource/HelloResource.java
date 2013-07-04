package perf.sample.resource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/names")

public class HelloResource {
    private static Logger logger = LoggerFactory.getLogger(HelloResource.class);

    /**
     * Get Agent Registration Information.
     * Mostly called by Loader-Server at its boot time to confirm the availability of agent
     * @return
     * @throws java.io.IOException
     */
    @Path("/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String welcome(@PathParam("name") String name) throws IOException {
        return String.format("{\"message\" : \"Hello %s\"}",name);
    }

}