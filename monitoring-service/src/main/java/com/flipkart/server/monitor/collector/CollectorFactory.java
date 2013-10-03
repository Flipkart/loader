package com.flipkart.server.monitor.collector;

import com.flipkart.perf.common.util.ClassHelper;
import com.flipkart.server.monitor.domain.OnDemandCollector;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Factory that builds Collector Instances using Reflection
 */
public class CollectorFactory {
    public static BaseCollector buildCollector(OnDemandCollector onDemandConfig) throws ClassNotFoundException,
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException {
        return (BaseCollector) ClassHelper.createInstance(Class.forName(onDemandConfig.getKlass()),
                new Class[]{
                        String.class,
                        Map.class,
                        int.class},
                new Object[]{
                        onDemandConfig.getName(),
                        onDemandConfig.getParams(),
                        onDemandConfig.getInterval()});
    }
}
