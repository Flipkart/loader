package com.flipkart.perf.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class SocketHelper {
    private static Logger logger = LoggerFactory.getLogger(SocketHelper.class);
    public static int getFreePort(int startPort, int endPort) {
        for(int port = startPort; port <= endPort; port++) {
            try {
                logger.info("Trying Port '"+port+"' to host graphs");
                new Socket("localhost",port);
            } catch (ConnectException ce) {
                return port;
            } catch (IOException e) {
            }
        }
        throw new RuntimeException("No Free Port");
    }
}
