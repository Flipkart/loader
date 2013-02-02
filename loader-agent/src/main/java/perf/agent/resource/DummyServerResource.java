package perf.agent.resource;

import com.yammer.metrics.annotation.Timed;
import org.apache.log4j.Logger;
import perf.agent.util.FileHelper;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/dummy/jobs")
public class DummyServerResource {
    private static Logger log = Logger.getLogger(DummyServerResource.class);


    @Path("/{jobId}/stats")
    @POST
    @Timed
    synchronized public void jobStats(@PathParam("jobId") String jobId,
                                      @QueryParam("file") String file,
                                      InputStream stats)
            throws IOException, InterruptedException {

        log.info("JobId :"+jobId);
        log.info("File :"+file);
        log.info("Stats :"+ FileHelper.readContent(stats));
    }
}

