package com.flipkart.perf.server.util;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 13/4/13
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectMapperUtil<T> {
    private static ObjectMapper objectMapper;

    public static ObjectMapper instance() {
        if(objectMapper == null) {
        	synchronized(ObjectMapperUtil.class) {
            	objectMapper = new ObjectMapper();
        	}
        }
        return objectMapper;
    }
}
