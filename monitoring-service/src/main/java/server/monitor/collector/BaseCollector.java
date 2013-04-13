package server.monitor.collector;

import com.open.perf.jackson.ObjectMapperUtil;
import org.apache.log4j.Logger;
import server.monitor.domain.ResourceCollectionInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

enum OS {
    SOLARIS, LINUX, HPUX , AIX, OSX, WINDOWS, UNKNOWN;
}

public abstract class BaseCollector{
    private String name;
    private int collectionInterval = 60000;
    private Map<String,Object> params;
    public static Map<String,Object> SYSTEM_PROPERTIES;
    public static OS CLIENT_OS;
    protected static Logger log = Logger.getLogger(BaseCollector.class);

    static {
        populateClistntOS();
        populateSystemProperties();
    }

    public BaseCollector(String name, Map<String, Object> params, int interval) {
        this.name = name;
        this.params = params;
        this.collectionInterval = interval;
    }

    public String getName() {
        return name;
    }

    private static void populateClistntOS() {
        final String osName = System.getProperty("os.name");
        if ((osName == null) || (osName.length() == 0))
        {
            throw new RuntimeException("Unable to determine client operating system");
        }

        final String lowerOSName = osName.toLowerCase();
        if (lowerOSName.equals("solaris") || lowerOSName.equals("sunos"))
        {
            CLIENT_OS = OS.SOLARIS;
        }
        else if (lowerOSName.equals("linux"))
        {
            CLIENT_OS = OS.LINUX;
        }
        else if (lowerOSName.equals("hp-ux") || lowerOSName.equals("hpux"))
        {
            CLIENT_OS = OS.HPUX;
        }
        else if (lowerOSName.equals("aix"))
        {
            CLIENT_OS = OS.AIX;
        }
        else if (lowerOSName.equals("mac os x"))
        {
            CLIENT_OS = OS.OSX;
        }
        else if (lowerOSName.contains("windows"))
        {
            CLIENT_OS = OS.WINDOWS;
        }
        else
        {
            CLIENT_OS = OS.UNKNOWN;
        }
    }

    private static void populateSystemProperties() {
        SYSTEM_PROPERTIES = new HashMap<String, Object>();
        Properties properties = System.getProperties();
        for(Object property : properties.keySet()) {
            SYSTEM_PROPERTIES.put(property.toString(), properties.get(property));
        }
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Object getParam(String param) {
        return this.params.get(param);
    }

    /**
     * Monitoring service would call this to check if this is supported or not.
     * Collector needs to writed there own logic to validate that they can be executed or not.
     * @return
     */
    final public ResourceCollectionInstance collect0() throws Exception {
        ResourceCollectionInstance collectionInstance = collect();
        log.debug(ObjectMapperUtil.instance().writeValueAsString(collectionInstance));
        return collectionInstance;
    }

    public int getCollectionInterval() {
        return collectionInterval;
    }

    public void setCollectionInterval(int collectionInterval) {
        this.collectionInterval = collectionInterval;
    }

    abstract public boolean supported();
    abstract public ResourceCollectionInstance collect() throws Exception;
}
