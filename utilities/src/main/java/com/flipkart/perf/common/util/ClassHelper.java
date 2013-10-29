package com.flipkart.perf.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility to create class and Function Instance using Reflection
 */
public class ClassHelper {

    public static <T> T createInstance(Class<T> type) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        return createInstance(type, new Class[]{}, new Object[]{});
    }

    public static <T> T createInstance(Class<T> type, Class[] paramTypes, Object[] params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Constructor<T> constructor = type.getConstructor(paramTypes);
        return constructor.newInstance(params);
    }

    public static Object getClassInstance(String className,Class[] paramTypes, Object[] params) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object  obj =   null;
        Class actionClassObj;
        actionClassObj      = Class.forName(className);
        Constructor cons    =   actionClassObj.getConstructor(paramTypes);
        obj                 =   cons.newInstance(params);
        return  obj;
    }

    public static <T> T getClassInstance(String className,Class[] paramTypes, Object[] params, Class<T> c) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object  obj =   null;
        Class actionClassObj;
        actionClassObj      =   Class.forName(className);
        Constructor cons    =   actionClassObj.getConstructor(paramTypes);
        obj                 =   cons.newInstance(params);
        return (T) obj;
    }

    public static Object getClassInstance(String className,Class[] paramTypes, Object[] params, ClassLoader customClassLoader) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object  obj =   null;
        Class actionClassObj;
        actionClassObj      =   customClassLoader.loadClass(className);//, false, customClassLoader);
        Constructor cons    =   actionClassObj.getConstructor(paramTypes);
        obj                 =   cons.newInstance(params);
        return  obj;
    }

    public static Method getMethod(String className, String functionName, Class[] paramTypes) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        Method method   =   null;
        Class actionClassObj;
        actionClassObj      =   Class.forName(className);
        method              =   actionClassObj.getDeclaredMethod(functionName, paramTypes);
        return method;
    }

    public static Method getMethod(String className, String functionName, Class[] paramTypes, ClassLoader customClassLoader) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        Method method   =   null;
        Class actionClassObj;
        actionClassObj      = Class.forName(className, false, customClassLoader);
        method              =   actionClassObj.getMethod(functionName, paramTypes);
        return method;
    }

}
