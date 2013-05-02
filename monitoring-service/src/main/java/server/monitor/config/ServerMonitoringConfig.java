package server.monitor.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 25/10/12
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */

import com.yammer.dropwizard.config.Configuration;
import java.util.List;

public class ServerMonitoringConfig extends Configuration {
    private String boxName;
    private List<OnDemandCollectorConfig> onDemandCollectors;

    public String getBoxName() {
        return boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
    }

    public List<OnDemandCollectorConfig> getOnDemandCollectors() {
        return onDemandCollectors;
    }

    public void setCollectors(List<OnDemandCollectorConfig> onDemandCollectorConfigs) {
        this.onDemandCollectors = onDemandCollectorConfigs;
    }
}
