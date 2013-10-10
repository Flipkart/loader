package com.flipkart.perf.server.domain;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 08/10/13
 * Time: 5:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class RunWorkFlowRequest {

    private String runName;
    private ArrayList<String> dependsOn;

    public String getRunName() {
        return runName;
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }

    public ArrayList<String> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(ArrayList<String> dependsOn) {
        this.dependsOn = dependsOn;
    }
}
