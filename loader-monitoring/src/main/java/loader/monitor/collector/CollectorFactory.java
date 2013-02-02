package loader.monitor.collector;

import loader.monitor.config.OnDemandCollectorConfig;
import loader.monitor.domain.OnDemandCollector;
import loader.monitor.util.ClassHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 4/1/13
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
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
