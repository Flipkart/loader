package server.monitor.config;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 4/1/13
 * Time: 12:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class OnDemandCollectorConfig {
    private String name;
    private String klass;
    private List<String> requiredParams;

    public String getName() {
        return name;
    }

    public OnDemandCollectorConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getKlass() {
        return klass;
    }

    public OnDemandCollectorConfig setKlass(String klass) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        this.klass = klass;
        return this;
    }

    public List<String> getRequiredParams() {
        return requiredParams;
    }

    public void setRequiredParams(List<String> requiredParams) {
        this.requiredParams = requiredParams;
    }
}