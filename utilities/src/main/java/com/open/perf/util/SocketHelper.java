package com.open.perf.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class SocketHelper {
    private static Logger log = Logger.getLogger(SocketHelper.class);
    public static int getFreePort(int startPort, int endPort) {
        for(int port = startPort; port <= endPort; port++) {
            try {
                log.info("Trying Port '"+startPort+"' to host graphs");
                new Socket("localhost",startPort);
            } catch (ConnectException ce) {
                return port;
            } catch (IOException e) {
            }
        }
        throw new RuntimeException("No Free Port");
    }
}
