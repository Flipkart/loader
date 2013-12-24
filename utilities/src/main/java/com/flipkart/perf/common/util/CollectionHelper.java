package com.flipkart.perf.common.util;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 11/12/12
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollectionHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> List<T> transformList(List list, final Class<T> clazz) {
        List<T> resultList = new ArrayList<T>(Collections2.transform(list, new Function<Map, T>() {
            @Override
            public T apply(Map arg0) {
                return MAPPER.convertValue(arg0, clazz);
            }
        }));
        return resultList;
    }
}
