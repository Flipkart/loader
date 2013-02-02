package loader.monitor.resource;

import com.yammer.metrics.annotation.Timed;
import loader.monitor.domain.PublisherRequest;
import loader.monitor.publisher.PublisherThread;
import org.apache.log4j.Logger;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/dummy")
public class DummyResource {
    private static Logger log = Logger.getLogger(DummyResource.class);


    @POST
    @Timed
    synchronized public void removeRequest(String stats) throws IOException, InterruptedException {
        log.info(stats);
    }
}

