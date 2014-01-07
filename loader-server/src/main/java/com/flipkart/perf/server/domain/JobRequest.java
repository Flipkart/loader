package com.flipkart.perf.server.domain;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 19/4/13
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class JobRequest {
    private String runName;
    private String runVersion;

    public String getRunName() {
        return runName;
    }

    public JobRequest setRunName(String runName) {
        this.runName = runName;
        return this;
    }

    public String getRunVersion() {
        return runVersion;
    }

    public JobRequest setRunVersion(String runVersion) {
        this.runVersion = runVersion;
        return this;
    }

}
