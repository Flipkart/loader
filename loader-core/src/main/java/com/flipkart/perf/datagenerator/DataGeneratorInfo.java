package com.flipkart.perf.datagenerator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 23/10/13
 * Time: 4:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataGeneratorInfo {
    public enum DataType {
        COUNTER, FIXED_VALUE, RANDOM_FLOAT, RANDOM_NUMBER, RANDOM_SELECTION, RANDOM_STRING, RANDOM_DISTRIBUTION;
    }

    private String generatorName;
    private DataType generatorType;
    private Map<String, Object> inputDetails;

    public DataGeneratorInfo() {
        this.inputDetails = new LinkedHashMap<String, Object>();
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public DataGeneratorInfo setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
        return this;
    }

    public DataType getGeneratorType() {
        return generatorType;
    }

    public DataGeneratorInfo setGeneratorType(DataType generatorType) {
        this.generatorType = generatorType;
        return this;
    }

    public Map<String, Object> getInputDetails() {
        return inputDetails;
    }

    public DataGeneratorInfo setInputDetails(Map<String, Object> inputDetails) {
        this.inputDetails = inputDetails;
        return this;
    }

    public DataGeneratorInfo addInput(String name, Object value) {
        this.inputDetails.put(name, value);
        return this;
    }
}
