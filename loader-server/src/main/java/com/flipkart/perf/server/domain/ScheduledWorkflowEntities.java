package com.flipkart.perf.server.domain;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 08/10/13
 * Time: 5:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScheduledWorkflowEntities {

    private String blockName;
    private String runName;
    private ArrayList<String> dependsOn;

    private String blockId;
    private String topPosition;
    private String leftPosition;

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

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getTopPosition() {
        return topPosition;
    }

    public void setTopPosition(String topPosition) {
        this.topPosition = topPosition;
    }

    public String getLeftPosition() {
        return leftPosition;
    }

    public void setLeftPosition(String leftPosition) {
        this.leftPosition = leftPosition;
    }
}
