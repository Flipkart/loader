package com.open.perf.function;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Class can be used by user/loader-ui to understand
 * what are the required parameters to make a particular function work properly
 */
public class FunctionParameter {
    private String name;
    private String description;
    private boolean mandatory;
    private Object defaultValue;

    public String getName() {
        return name;
    }

    public FunctionParameter setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FunctionParameter setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public FunctionParameter setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public FunctionParameter setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return super.toString();
    }
}
