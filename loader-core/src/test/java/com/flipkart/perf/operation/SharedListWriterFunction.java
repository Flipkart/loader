package com.flipkart.perf.operation;

import com.flipkart.perf.core.FunctionContext;
import com.flipkart.perf.function.PerformanceFunction;
import com.flipkart.perf.inmemorydata.SharedDataInfo;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Can be used to introduce Delay in the Group
 */
public class SharedListWriterFunction extends PerformanceFunction {
    public static final String SC_LIST_NAME = "test-queue";
    public static final String IP_QUEUE_ELEMENT = "queue-element";
    private List list;
    private static Integer count = 0;
    @Override
    public void init(FunctionContext context) {
        list = context.getSharedList(SC_LIST_NAME);
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        String element = context.getParameterAsString(IP_QUEUE_ELEMENT) ;
        synchronized (count) {
            element += count;
            count++;
            logger.info("Counter : "+count);
        }
        synchronized (list) {
            list.add(element);
        }
        logger.info("Queue Size after adding element :" + list.size());
    }

    @Override
    public LinkedHashMap<String, SharedDataInfo> sharedData(){
        LinkedHashMap<String, SharedDataInfo> sharedCollections = new LinkedHashMap<String, SharedDataInfo>();
        sharedCollections.put(SC_LIST_NAME, SharedDataInfo.sharedList(SC_LIST_NAME, String.class));
        return sharedCollections;
    }
}
