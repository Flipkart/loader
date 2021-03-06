package com.flipkart.perf.server.domain;

import com.flipkart.perf.domain.Load;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a Performance LoadPart in a Performance Run
 */
public class LoadPart {
    private String name;
    private int agents;
    private List<String> classes;
    private List<String> inputFileResources = new ArrayList<String>();
    private Load load;
    private Set<String> agentTags;

    public LoadPart() {
        agentTags = new LinkedHashSet<String>();
    }

    public String getName() {
        return name;
    }

    public LoadPart setName(String name) {
        this.name = name;
        return this;
    }

    public int getAgents() {
        return agents;
    }

    public LoadPart setAgents(int agents) {
        this.agents = agents;
        return this;
    }

    public List<String> getClasses() {
        return classes;
    }

    public LoadPart setClasses(List<String> classes) {
        this.classes = classes;
        return this;
    }

    public Load getLoad() {
        return load;
    }

    public LoadPart setLoad(Load load) {
        this.load = load;
        return this;
    }

    public List<String> getInputFileResources() {
        return inputFileResources;
    }

    public LoadPart setInputFileResources(List<String> inputFileResources) {
        this.inputFileResources = inputFileResources;
        return this;
    }

    public Set<String> getAgentTags() {
        return agentTags;
    }

    public void setAgentTags(Set<String> agentTags) {
        this.agentTags = agentTags;
    }
}
