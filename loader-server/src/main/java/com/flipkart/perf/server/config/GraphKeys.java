package com.flipkart.perf.server.config;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 18/11/13
 * Time: 1:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class GraphKeys {
    private String key;
    private String name;
    private String color;
    private boolean isRegex;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean getIsRegex() {
        return isRegex;
    }

    public void setIsRegex(boolean isRegex) {
        this.isRegex = isRegex;
    }
}
