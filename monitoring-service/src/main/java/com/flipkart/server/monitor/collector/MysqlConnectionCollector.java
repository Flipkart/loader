package com.flipkart.server.monitor.collector;

import com.flipkart.perf.common.util.SQLHelper;
import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.flipkart.server.monitor.domain.ResourceCollectionInstance;

/**
 * Collects Currently used mysql connections, group them by connectingHost.connected db
 */
public class MysqlConnectionCollector extends BaseCollector {

    public MysqlConnectionCollector(String name, Map<String, Object> params, int interval) {
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
        String user = this.getParam("user").toString();
        String pass = this.getParam("password").toString();
        String db = this.getParam("db").toString();

        int totalConnections = 0;
        SQLHelper sqlHelper = new SQLHelper(host, port, user, pass, db);
        Map<String, Double> connectionsStats = new HashMap<String, Double>();
        try {
            ResultSet rs = sqlHelper.executeQuery("show processlist");
            while(rs.next()) {
                String connectingHost = rs.getString(3).split(":")[0].trim();
                String connectedDB = rs.getString(4);
                String hostDBConnection = connectingHost+"."+connectedDB;
                Double dbConnections = connectionsStats.get(hostDBConnection);
                if(dbConnections == null)
                    dbConnections = 0d;

                dbConnections++;

                connectionsStats.put(hostDBConnection, dbConnections);
                totalConnections++;
            }
            connectionsStats.put("total.connections", (double) totalConnections);
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        finally {
            sqlHelper.closeConnection();
        }

        return collectionInstance.
                setTime(System.currentTimeMillis()).
                setMetrics(connectionsStats);
    }
}