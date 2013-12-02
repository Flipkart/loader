package com.flipkart.perf.server.domain;

import com.flipkart.perf.function.FunctionParameter;
import com.flipkart.perf.inmemorydata.SharedDataInfo;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Hold information about deployed Performance function
 */
public class FunctionInfo {
    private String function;
    private LinkedHashMap<String, FunctionParameter> inputParameters;
    private LinkedHashMap<String, FunctionParameter> outputParameters;
    private List<String> description;
    private List<String> customTimers;
    private List<String> customCounters;
    private List<String> customHistograms;
    private LinkedHashMap<String, SharedDataInfo> sharedData;

    public String getFunction() {
        return function;
    }

    public FunctionInfo setFunction(String function) {
        this.function = function;
        return this;
    }

    public LinkedHashMap<String, FunctionParameter> getInputParameters() {
        return inputParameters;
    }

    public FunctionInfo setInputParameters(LinkedHashMap<String, FunctionParameter> inputParameters) {
        this.inputParameters = inputParameters;
        return this;
    }

    public LinkedHashMap<String, FunctionParameter> getOutputParameters() {
        return outputParameters;
    }

    public FunctionInfo setOutputParameters(LinkedHashMap<String, FunctionParameter> outputParameters) {
        this.outputParameters = outputParameters;
        return this;
    }

    public List<String> getDescription() {
        return description;
    }

    public FunctionInfo setDescription(List<String> description) {
        this.description = description;
        return this;
    }

    public List<String> getCustomTimers() {
        return customTimers;
    }

    public FunctionInfo setCustomTimers(List<String> customTimers) {
        this.customTimers = customTimers;
        return this;
    }

    public List<String> getCustomCounters() {
        return customCounters;
    }

    public FunctionInfo setCustomCounters(List<String> customCounters) {
        this.customCounters = customCounters;
        return this;
    }

    public List<String> getCustomHistograms() {
        return customHistograms;
    }

    public FunctionInfo setCustomHistograms(List<String> customHistograms) {
        this.customHistograms = customHistograms;
        return this;
    }

    public void setSharedData(LinkedHashMap<String, SharedDataInfo> sharedData) {
        this.sharedData = sharedData;
    }

    public LinkedHashMap<String, SharedDataInfo> getSharedData() {
        return sharedData;
    }
}
