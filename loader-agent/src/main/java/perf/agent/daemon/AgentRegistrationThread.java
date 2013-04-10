package perf.agent.daemon;

import com.open.perf.util.Clock;
import perf.agent.client.LoaderServerClient;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 12/2/13
 * Time: 2:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgentRegistrationThread extends Thread{
    private boolean registered = false;
    private int registrationInterval = 10000;
    private final LoaderServerClient serverClient;
    private final Map<String, Object> registrationParams;
    private static AgentRegistrationThread registrationThread;

    private AgentRegistrationThread(LoaderServerClient serverClient, Map<String,Object> registrationParams) {
        this.serverClient = serverClient;
        this.registrationParams = registrationParams;
    }

    public static AgentRegistrationThread initialize(LoaderServerClient serverClient, Map<String,Object> registrationParams) {
        if(registrationThread == null) {
            registrationThread = new AgentRegistrationThread(serverClient, registrationParams);
            registrationThread.start();
        }
        return registrationThread;
    }

    public static AgentRegistrationThread getInstance() {
        return registrationThread;
    }

    public void run() {
        while(true) {
            if(!registered) {
                try {
                    serverClient.register(this.registrationParams);
                    registered = true;
                } catch (ExecutionException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            try {
                Clock.sleep(registrationInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
