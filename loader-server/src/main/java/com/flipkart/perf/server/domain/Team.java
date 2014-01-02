package com.flipkart.perf.server.domain;

import com.flipkart.perf.server.util.ResponseBuilder;

import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 19/7/13
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class Team {
    private String name;
    private List<String> runs = new ArrayList<String>();

    public String getName() {
        return name;
    }

    public Team setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getRuns() {
        return runs;
    }

    public Team setRuns(List<String> runs) {
        this.runs = runs;
        return this;
    }

    public String runExistsOrException(String runName) {
        if(!this.runs.contains(runName))
            throw new WebApplicationException(ResponseBuilder.resourceNotFound("Run", runName));
        return runName;
    }
}
