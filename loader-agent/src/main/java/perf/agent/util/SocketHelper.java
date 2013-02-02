package perf.agent.util;

import org.apache.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 30/12/12
 * Time: 7:34 PM
 * To change this template use File | Settings | File Templates.
 */
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
        throw new WebApplicationException(400);
    }
}
