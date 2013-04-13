package com.open.perf.jackson;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 13/4/13
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectMapperUtil {
    private static ObjectMapper objectMapper;

    public static ObjectMapper instance() {
        if(objectMapper == null)
            objectMapper = new ObjectMapper();
        return objectMapper;
    }
}
