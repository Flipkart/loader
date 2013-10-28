package com.flipkart.perf.operation;

import com.flipkart.perf.core.FunctionContext;
import com.flipkart.perf.function.PerformanceFunction;
import com.flipkart.perf.inmemorydata.SharedDataInfo;

import java.util.*;

/**
 * Can be used to introduce Delay in the Group
 */
public class SharedListReaderFunction extends PerformanceFunction {
    public static final String SC_LIST_NAME = "test-queue";
    private List<String> list;
    @Override
    public void init(FunctionContext context) {
        list = context.getSharedList(SC_LIST_NAME);
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        synchronized (list) {
            String element = list.remove(0);
        }
        logger.info("Queue Size after removing element :" + list.size());
    }

    @Override
    public LinkedHashMap<String, SharedDataInfo> sharedData(){
        LinkedHashMap<String, SharedDataInfo> sharedCollections = new LinkedHashMap<String, SharedDataInfo>();
        sharedCollections.put(SC_LIST_NAME, SharedDataInfo.sharedList(SC_LIST_NAME, String.class));
        return sharedCollections;
    }
}
