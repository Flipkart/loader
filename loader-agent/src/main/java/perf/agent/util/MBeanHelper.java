package perf.agent.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MBeanHelper {
    public static Map<String, Object> getOSParams() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String, Object> osParams = new LinkedHashMap<String, Object>();
        OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();
        Class clazz = Class.forName("com.sun.management.UnixOperatingSystemMXBean");
        Map<String, String> functionParamMap = new LinkedHashMap<String, String>();
        functionParamMap.put("getName", "linux");
        functionParamMap.put("getArch", "architecture");
        functionParamMap.put("getAvailableProcessors", "processors");
        functionParamMap.put("getTotalPhysicalMemorySize", "memory");
        functionParamMap.put("getTotalSwapSpaceSize", "swap");

        for(String function : functionParamMap.keySet()) {
            osParams.put(functionParamMap.get(function), clazz.getMethod(function).invoke(mxBean));
        }
        return osParams;
    }
}
