package com.flipkart.perf.server.domain;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 2/1/14
 * Time: 4:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class RunMeta {
    private int latestVersion = 1;

    public int getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(int latestVersion) {
        this.latestVersion = latestVersion;
    }
}
