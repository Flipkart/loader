package com.flipkart.perf.agent.cache;

import com.flipkart.perf.agent.config.ResourceStorageFSConfig;

import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Maintain information about the libraries deployed on the agent
 * Which is further used in forking loader process to do performance run
 */
public class LibCache {
    private Map<String,String> classLibMap;
    private String platformLibClassPath;
    private static LibCache self;
    private ResourceStorageFSConfig storageConfig;

    private LibCache(ResourceStorageFSConfig storageConfig) throws IOException {
        this.storageConfig = storageConfig;
        this.classLibMap = new HashMap<String, String>();
        this.platformLibClassPath = "";
        refreshClassLibMap();
        refreshPlatformLib();
    }

    public static LibCache initialize(ResourceStorageFSConfig storageConfig) throws IOException {
        if(self == null)
            self = new LibCache(storageConfig);
        return self;
    }

    public static LibCache getInstance() {
        return self;
    }

    public void refreshClassLibMap() throws IOException {
        synchronized (classLibMap) {
            classLibMap.clear();

            Properties prop = new Properties();
            InputStream mappingFileIS = new FileInputStream(storageConfig.getMappingFile());
            try {
                prop.load(mappingFileIS);
                for(String className : prop.stringPropertyNames()) {
                    classLibMap.put(className, prop.get(className).toString());
                }
            }
            catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            finally {
                mappingFileIS.close();
            }
        }
    }

    public void refreshPlatformLib() {
        synchronized (platformLibClassPath) {
            File platformLibPath = new File(storageConfig.getPlatformLibPath());
            platformLibClassPath = "";
            for(File libFile : platformLibPath.listFiles()) {
                platformLibClassPath += libFile.getAbsolutePath() + File.pathSeparator;
            }
            platformLibClassPath = platformLibClassPath.length() > 0 ?
                                        platformLibClassPath.substring(0,platformLibClassPath.length()-1) :
                                        platformLibClassPath;
        }
    }

    public String buildJobClassPath(List<String> classList) throws IOException {
        Set<String> libs = new HashSet<String>();
        String classPath = platformLibClassPath + File.pathSeparator;

        for(String className : classList) {
            String libPath = classLibMap.get(className);
            if(libPath == null)
                throw new WebServiceException("Library for class '"+className+"' not found. Server needs to deploy related library first");

            if(!libs.contains(libPath)) {
                libs.add(libPath);
                classPath += libPath + File.pathSeparator;
            }
        }

        return classPath.length() > 0 ?
                classPath.substring(0,classPath.length()-1) :
                classPath;
    }

    public List getPlatformLibs() {
        return Arrays.asList(platformLibClassPath.split(File.pathSeparator));
    }

    public Map getLibsMapWithLibAsKey() {
        Map<String,List<String>> map = new HashMap<String,List<String>>();

        for(String className : classLibMap.keySet()) {
            String lib = classLibMap.get(className);

            List<String> classList = map.get(lib);
            if(classList == null)
                classList = new ArrayList<String>();

            classList.add(className);

            map.put(lib, classList);
        }

        return map;
    }

    public Map getLibsMapWithClassAsKey() {
        return classLibMap;
    }

    public List classesExist(String[] classes) {
        List<String> classesNotDeployed = new ArrayList<String>();
        for(String className : classes)
            if(!classLibMap.containsKey(className))
                classesNotDeployed.add(className);
        return classesNotDeployed;
    }
}
