package com.open.perf.domain;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 31/1/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobPart {
    private String agent;
    private Loader loader;

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public Loader getLoader() {
        return loader;
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }
}
