package com.open.perf.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 29/1/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassHelper {
    public static Object getClassInstance(String className,Class[] paramTypes, Object[] params) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object  obj =   null;
        Class actionClassObj;
        actionClassObj      = Class.forName(className);
        Constructor cons    =   actionClassObj.getConstructor(paramTypes);
        obj                 =   cons.newInstance(params);
        return  obj;
    }

    public static Method getMethod(String className, String functionName, Class[] paramTypes) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        Method method   =   null;
        Class actionClassObj;
        actionClassObj      = Class.forName(className);
        method              =   actionClassObj.getDeclaredMethod(functionName, paramTypes);
        return method;
    }

}
