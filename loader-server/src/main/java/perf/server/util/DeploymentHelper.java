package perf.server.util;

import com.open.perf.util.FileHelper;
import org.apache.log4j.Logger;
import perf.server.client.LoaderAgentClient;
import perf.server.config.AgentConfig;
import perf.server.config.LibStorageFSConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 8/4/13
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeploymentHelper {
    private static DeploymentHelper myInstance;
    private final AgentConfig agentConfig;
    private final LibStorageFSConfig libStorageConfig;

    private DeploymentHelper(AgentConfig agentConfig, LibStorageFSConfig libStorageConfig) {
        this.agentConfig = agentConfig;
        this.libStorageConfig = libStorageConfig;
    }

    public static DeploymentHelper initialize(AgentConfig agentConfig, LibStorageFSConfig libStorageConfig) {
        if(myInstance == null)
            myInstance = new DeploymentHelper(agentConfig, libStorageConfig);
        return myInstance;
    }

    public static DeploymentHelper instance() {
        return myInstance;
    }

    private static Logger log = Logger.getLogger(DeploymentHelper.class);

    /**
     * Deploy platform libs on agent if they are not already deployed at all or if new platform libs not deployed
     * @param agentIP
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void deployPlatformLibsOnAgent(String agentIP) throws IOException, ExecutionException, InterruptedException {
        deployPlatformLibsOnAgent(agentIP, false);
    }

    /**
     * Would forcefully deploy platform libs on agent if force = true
     * @param agentIP
     * @param force
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void deployPlatformLibsOnAgent(String agentIP, boolean force) throws IOException, ExecutionException, InterruptedException {
        String agentPlatformInfoFile = this.agentConfig.getAgentPlatformLibInfoFile(agentIP);
        boolean deployPlatformLib;
        File platformFile = new File(agentPlatformInfoFile);

        if(force)
            deployPlatformLib = true;
        else {
            if(platformFile.exists()) {
                Properties prop = new Properties();
                prop.load(new FileInputStream(platformFile));
                if(prop.get("deploymentTime") != null) {
                    long deploymentTimeInAgent = Long.parseLong(prop.get("deploymentTime").toString());
                    long deploymentTimeInServer = new File(this.libStorageConfig.getPlatformLibPath()).lastModified();
                    deployPlatformLib = deploymentTimeInServer > deploymentTimeInAgent;
                }
                else
                    deployPlatformLib = true;
            }
            else {
                deployPlatformLib = true;
            }
        }

        if(deployPlatformLib) {
            log.info("Deploying Platform Lib on Agent "+agentIP);
            Properties prop = new Properties();
            new LoaderAgentClient(agentIP,
                    agentConfig.getAgentPort()).deployPlatformLibs();
            prop.put("deploymentTime", String.valueOf(System.currentTimeMillis()));
            FileHelper.createFilePath(platformFile.getAbsolutePath());
            prop.store(new FileOutputStream(platformFile), "Platform Lib Information");
        }
    }
}
