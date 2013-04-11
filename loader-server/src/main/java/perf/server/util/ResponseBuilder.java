package perf.server.util;

import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 9/2/13
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResponseBuilder {
    public static Response response(Response.Status status, Object message) {
        return Response.status(status).entity(message).build();
    }

    public static Response jobNotOver(String jobId) {
        return ResponseBuilder.response(Response.Status.BAD_REQUEST,
                String.format("{\"reason\" : \"Job %s Not Over Yet\"}", jobId));
    }

    public static Response jobNotFound(String jobId) {
        return ResponseBuilder.response(Response.Status.NOT_FOUND,
                String.format("{\"reason\" : \"Job %s doesn't exist\"}", jobId));
    }

    public static Response agentNotRegistered(String agentIp) {
        return ResponseBuilder.response(Response.Status.NOT_FOUND,
                String.format("{\"reason\" : \"Agent %s not registered yet\"}", agentIp));
    }

    public static Response runNameAlreadyExists(String runName) {
        return ResponseBuilder.response(Response.Status.CONFLICT,
                String.format("{\"reason\" : \"Run %s Already exists\"}", runName));
    }

    public static Response runNameDoesNotExist(String runName) {
        return ResponseBuilder.response(Response.Status.NOT_FOUND,
                String.format("{\"reason\" : \"Run %s Does Not exist\"}", runName));
    }
}

