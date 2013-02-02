package loader.monitor.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 3/12/12
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassHelper {
    public static <T> T createInstance(Class<T> type) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        return createInstance(type, new Class[]{}, new Object[]{});
    }

    public static <T> T createInstance(Class<T> type, Class[] paramTypes, Object[] params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Constructor<T> constructor = type.getConstructor(paramTypes);
        return constructor.newInstance(params);
    }
}