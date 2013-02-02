package com.open.perf.operations;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

abstract public class Operation {
    protected static Logger logger = Logger.getLogger(Operation.class);
    private Map<String,Object> params = new HashMap<String, Object>();

    public Map<String, Object> getParams() {
        return params;
    }

    public Operation setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public Operation addParams(String param, Object value) {
        this.params.put(param, value);
        return this;
    }
}