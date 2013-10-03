package com.flipkart.perf.agent.util;

import javax.ws.rs.core.Response;

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
}

