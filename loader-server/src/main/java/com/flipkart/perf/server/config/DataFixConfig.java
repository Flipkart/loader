package com.flipkart.perf.server.config;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 30/6/13
 * Time: 6:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataFixConfig {
    private String dataFixersFile;
    private String doneFixersFile;

    public String getDataFixersFile() {
        return dataFixersFile;
    }

    public void setDataFixersFile(String dataFixersFile) {
        this.dataFixersFile = dataFixersFile;
    }

    public String getDoneFixersFile() {
        return doneFixersFile;
    }

    public void setDoneFixersFile(String doneFixersFile) {
        this.doneFixersFile = doneFixersFile;
    }
}
