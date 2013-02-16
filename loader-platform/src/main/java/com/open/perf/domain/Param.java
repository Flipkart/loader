package com.open.perf.domain;

import org.codehaus.jackson.annotate.JsonProperty;

public class Param {
	@JsonProperty
	private String name;
	@JsonProperty
    private Object value;

    public Param setName(String key) {
        this.name    = key;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Param setValue(Object value) {
        this.value    = value;
        return this;
    }

    public Object getValue() {
        return this.value;
    }

    public String toString(){
        return this.name+"="+this.value+"\n";
    }

    public Param clone() {
        Param newParam =   new Param();
        newParam.setName(this.name);
        newParam.setValue(this.value);
        return newParam;
    }

}
