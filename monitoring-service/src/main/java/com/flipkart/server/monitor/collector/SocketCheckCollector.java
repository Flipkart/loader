package com.flipkart.server.monitor.collector;

import com.flipkart.server.monitor.domain.ResourceCollectionInstance;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * nitinka
 * Checks if intended socket is up or not. Returns 0 and 1 based on if socket is up or down respectively
 */
public class SocketCheckCollector extends BaseCollector {

    public SocketCheckCollector(String name, Map<String, Object> params, int interval) {
        super(name, params, interval);
    }

    @Override
    public boolean supported() {
        return true;
    }

    @Override
    public ResourceCollectionInstance collect() throws SQLException, ClassNotFoundException {
        ResourceCollectionInstance collectionInstance = new ResourceCollectionInstance().
                setResourceName(this.getName());

        String host = this.getParam("host").toString();
        int port = Integer.parseInt(this.getParam("port").toString());

        Map<String, Double> connectionsStats = new HashMap<String, Double>();
        try {
            Socket soc = new Socket(host, port);
            soc.close();
            connectionsStats.put("up",1.0);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            connectionsStats.put("up",0.0);
        }

        return collectionInstance.
                setTime(System.currentTimeMillis()).
                setMetrics(connectionsStats);
    }
}