package perf.server.util;

import com.open.perf.util.FileHelper;
import org.apache.log4j.Logger;
import perf.server.cache.LibCache;
import perf.server.client.LoaderAgentClient;
import perf.server.config.AgentConfig;
import perf.server.config.LibStorageFSConfig;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 8/4/13
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeploymentHelper {
    private static Logger log = Logger.getLogger(DeploymentHelper.class);
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
            if(new LoaderAgentClient(agentIP,
                    agentConfig.getAgentPort()).deployPlatformLibs()) {
                prop.put("deploymentTime", String.valueOf(System.currentTimeMillis()));
                FileHelper.createFilePath(platformFile.getAbsolutePath());
                prop.store(new FileOutputStream(platformFile), "Platform Lib Information");
            }
            else {
                log.error("Platform Lib Deployment Failed on Agent "+agentIP);
                throw new IOException("Platform Lib Deployment Failed on Agent "+agentIP);
            }
        }
    }

    public void deployClassLibsOnAgent(String agentIP, String classListStr) throws IOException, ExecutionException, InterruptedException {
        deployClassLibsOnAgent(agentIP, classListStr, false);
    }

    public void deployClassLibsOnAgent(String agentIP, String classListStr, boolean force) throws IOException, ExecutionException, InterruptedException {
        Map<String, String> libClassListMap = makeLibClassListMap(classListStr);
        String agentClassLibInfoFile = this.agentConfig.getAgentClassLibInfoFile(agentIP);
        File classLibDeploymentFile = new File (agentClassLibInfoFile);

        Properties prop = new Properties();
        if(classLibDeploymentFile.exists()) {
            prop.load(new FileInputStream(classLibDeploymentFile.getAbsolutePath()));
            boolean deployLib;

            for(String lib : libClassListMap.keySet()) {
                Object libDeploymentTimeAgentObj = prop.get(lib);
                if(libDeploymentTimeAgentObj == null)
                    deployLib = true;
                else {
                    long libDeploymentTimeAgent = Long.parseLong(libDeploymentTimeAgentObj.toString());
                    long libDeploymentTimeServer = new File(lib).lastModified();
                    deployLib = libDeploymentTimeServer > libDeploymentTimeAgent;
                }

                if(deployLib || force) {
                    if(new LoaderAgentClient(agentIP,agentConfig.getAgentPort()).
                            deployClassLibs(lib,
                                    libClassListMap.get(lib))) {
                        prop.put(lib, String.valueOf(System.currentTimeMillis()));
                    }
                    else {
                        log.error("Class Lib Deployment Failed on Agent "+agentIP);
                        throw new IOException("Class Lib Deployment Failed on Agent "+agentIP);
                    }
                }
            }
        }
        else {
            FileHelper.createFilePath(classLibDeploymentFile.getAbsolutePath());
            for(String lib : libClassListMap.keySet()) {
                if(new LoaderAgentClient(agentIP,agentConfig.getAgentPort()).
                        deployClassLibs(lib,
                                libClassListMap.get(lib))) {
                    prop.put(lib, String.valueOf(System.currentTimeMillis()));
                }
                else {
                    log.error("Class Lib Deployment Failed on Agent "+agentIP);
                    throw new IOException("Class Lib Deployment Failed on Agent "+agentIP);
                }
            }
        }
        prop.store(new FileOutputStream(classLibDeploymentFile), "ClassLib Deployment times");
    }


    private Map<String, String> makeLibClassListMap(String classListStr) {
        Map<String,String> libClassListMap = new HashMap<String, String>();
        List<String> libsRequired = new ArrayList<String>();
        for(String className : classListStr.split("\n")) {
            libsRequired.add(LibCache.instance().getLibsMapWithClassAsKey().get(className));
        }

        for(String libRequired : libsRequired) {
            String libClassListStr = "";
            List<String> libClassList = LibCache.instance().getLibsMapWithLibAsKey().get(libRequired);
            for(String libClass : libClassList)
                libClassListStr += libClass + "\n";
            libClassListMap.put(libRequired, libClassListStr.trim());
        }
        return libClassListMap;
    }

}
