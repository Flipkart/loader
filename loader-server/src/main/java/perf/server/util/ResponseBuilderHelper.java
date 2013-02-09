package perf.server.util;

import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 9/2/13
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResponseBuilderHelper {
    public static Response response(Response.Status status, Object message) {
        return Response.status(status).entity(message).build();
    }

    public static Response jobNotOver(String jobId) {
        return ResponseBuilderHelper.response(Response.Status.BAD_REQUEST,
                            String.format("{\"reason\" : \"Job %s Not Over Yet\"}", jobId));
    }

    public static Response jobNotFound(String jobId) {
        return ResponseBuilderHelper.response(Response.Status.NOT_FOUND,
                String.format("{\"reason\" : \"Job %s doesn't exist\"}",jobId));
    }
}

