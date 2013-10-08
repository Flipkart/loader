package com.flipkart.perf.agent.util;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SystemInfo {
    private static OperatingSystemMXBean osMXBean;
    static {
        osMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    public static Map<String, Object> getOSParams() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String, Object> osParams = new LinkedHashMap<String, Object>();
        osParams.put("linux", osMXBean.getName());
        osParams.put("architecture", osMXBean.getArch());
        osParams.put("processors", osMXBean.getAvailableProcessors());
        osParams.put("memory", osMXBean.getTotalPhysicalMemorySize());
        osParams.put("swap", osMXBean.getTotalSwapSpaceSize());
        return osParams;
    }

    public static String getName() {
        return osMXBean.getName();
    }

    public static int getProcessors() {
        return osMXBean.getAvailableProcessors();
    }

    public static String getArchitecture() {
        return osMXBean.getArch();
    }

    public static long getTotalPhysicalMemorySize() {
        return osMXBean.getTotalPhysicalMemorySize();
    }

    public static long getTotalSwapSize() {
        return osMXBean.getTotalSwapSpaceSize();
    }

    public static double getSystemLoadAverage() {
        return osMXBean.getSystemLoadAverage();
    }
}
