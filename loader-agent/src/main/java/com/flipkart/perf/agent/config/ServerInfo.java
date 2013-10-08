package com.flipkart.perf.agent.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/1/13
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerInfo {
    private String host;
    private int port;

    public String getHost() {
        return host;
    }

    public ServerInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ServerInfo setPort(int port) {
        this.port = port;
        return this;
    }
}
