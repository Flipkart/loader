package com.open.perf.domain;

import org.codehaus.jackson.annotate.JsonProperty;

public class ParamBean {
	@JsonProperty
	private String name;
	@JsonProperty
    private Object value;

    public ParamBean setName(String key) {
        this.name    = key;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public ParamBean setValue(Object value) {
        this.value    = value;
        return this;
    }

    public Object getValue() {
        return this.value;
    }

    public String toString(){
        return this.name+"="+this.value+"\n";
    }

    public ParamBean clone() {
        ParamBean newParamBean  =   new ParamBean();
        newParamBean.setName(this.name);
        newParamBean.setValue(this.value);
        return newParamBean;
    }

}
