package com.flipkart.perf.server.util;

import com.flipkart.perf.common.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.flipkart.perf.server.cache.LibCache;
import com.flipkart.perf.server.client.LoaderAgentClient;
import com.flipkart.perf.server.config.AgentConfig;
import com.flipkart.perf.server.config.ResourceStorageFSConfig;
import com.flipkart.perf.server.exception.LibNotDeployedException;

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
    private static Logger logger = LoggerFactory.getLogger(DeploymentHelper.class);
    private static DeploymentHelper myInstance;
    private final AgentConfig agentConfig;
    private final ResourceStorageFSConfig resourceStorageFSConfig;

    private DeploymentHelper(AgentConfig agentConfig, ResourceStorageFSConfig resourceStorageFSConfig) {
        this.agentConfig = agentConfig;
        this.resourceStorageFSConfig = resourceStorageFSConfig;
    }

    public static DeploymentHelper initialize(AgentConfig agentConfig, ResourceStorageFSConfig resourceStorageFSConfig) {
        if(myInstance == null)
            myInstance = new DeploymentHelper(agentConfig, resourceStorageFSConfig);
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
    public void deployPlatformLibsOnAgent(String agentIP) throws IOException, ExecutionException, InterruptedException, LibNotDeployedException {
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
    public void deployPlatformLibsOnAgent(String agentIP, boolean force) throws IOException, ExecutionException, InterruptedException, LibNotDeployedException {
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
                    long deploymentTimeInServer = new File(this.resourceStorageFSConfig.getPlatformLibPath()).lastModified();
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
            logger.info("Deploying Platform Lib on Agent "+agentIP);
            Properties prop = new Properties();
            if(new LoaderAgentClient(agentIP,
                    agentConfig.getAgentPort()).deployPlatformLibs()) {
                prop.put("deploymentTime", String.valueOf(System.currentTimeMillis()));
                FileHelper.createFilePath(platformFile.getAbsolutePath());
                prop.store(new FileOutputStream(platformFile), "Platform Lib Information");
            }
            else {
                logger.error("Platform Lib Deployment Failed on Agent "+agentIP);
                throw new IOException("Platform Lib Deployment Failed on Agent "+agentIP);
            }
        }
    }

    public void deployUDFLibsOnAgent(String agentIP, String classListStr) throws IOException, ExecutionException, InterruptedException, LibNotDeployedException {
        deployUDFLibsOnAgent(agentIP, classListStr, false);
    }

    public void deployUDFLibsOnAgent(String agentIP, String classListStr, boolean force) throws IOException, ExecutionException, InterruptedException, LibNotDeployedException {
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
                            deployUDFLib(lib,
                                    libClassListMap.get(lib))) {
                        prop.put(lib, String.valueOf(System.currentTimeMillis()));
                    }
                    else {
                        logger.error("Class Lib Deployment Failed on Agent "+agentIP);
                        throw new IOException("Class Lib Deployment Failed on Agent "+agentIP);
                    }
                }
            }
        }
        else {
            FileHelper.createFilePath(classLibDeploymentFile.getAbsolutePath());
            for(String lib : libClassListMap.keySet()) {
                if(new LoaderAgentClient(agentIP,agentConfig.getAgentPort()).
                        deployUDFLib(lib,
                                libClassListMap.get(lib))) {
                    prop.put(lib, String.valueOf(System.currentTimeMillis()));
                }
                else {
                    logger.error("Class Lib Deployment Failed on Agent "+agentIP);
                    throw new IOException("Class Lib Deployment Failed on Agent "+agentIP);
                }
            }
        }
        prop.store(new FileOutputStream(classLibDeploymentFile), "ClassLib Deployment times");
    }

    public void deployInputFilesOnAgent(String agentIP, List<String> inputFileResourceNames) throws IOException, ExecutionException, InterruptedException, LibNotDeployedException {
        for(String inputFileResourceName : inputFileResourceNames) {
            String inputFileResourcePath = resourceStorageFSConfig.getInputFilePath(inputFileResourceName);
            Map agentDeploymentInfoMap = ObjectMapperUtil.instance().
                    readValue(new File(resourceStorageFSConfig.getInputFileAgentDeploymentPath(inputFileResourceName)), Map.class);
            boolean toDeploy = true;
            if(agentDeploymentInfoMap.containsKey(agentIP)) {
                toDeploy = Long.parseLong(agentDeploymentInfoMap.get(agentIP).toString()) < new File(inputFileResourcePath).lastModified();
            }

            if(toDeploy) {
                logger.info("Deploying Input File Resource '"+inputFileResourceName+"' on agent "+agentIP);
                if(new LoaderAgentClient(agentIP,agentConfig.getAgentPort()).
                        deployInputFile(inputFileResourceName, inputFileResourcePath)) {
                    agentDeploymentInfoMap.put(agentIP, System.currentTimeMillis());
                    ObjectMapperUtil.instance().writerWithDefaultPrettyPrinter().
                            writeValue(new File(resourceStorageFSConfig.getInputFileAgentDeploymentPath(inputFileResourceName)), agentDeploymentInfoMap);
                }
                else {
                    logger.error("Input File Resource '"+inputFileResourceName+"' Failed on Agent "+agentIP);
                    throw new IOException("Input File Resource '"+inputFileResourceName+"' Failed on Agent "+agentIP);
                }
            }
        }
    }

    private Map<String, String> makeLibClassListMap(String classListStr) throws LibNotDeployedException {
        Map<String,String> libClassListMap = new HashMap<String, String>();
        Set<String> libsRequired = new HashSet<String>();
        for(String className : classListStr.split("\n")) {
            if(LibCache.instance().getLibsMapWithClassAsKey().containsKey(className))
                libsRequired.add(LibCache.instance().getLibsMapWithClassAsKey().get(className));
            else
                throw new LibNotDeployedException("No Library Deployed for class "+className);
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
