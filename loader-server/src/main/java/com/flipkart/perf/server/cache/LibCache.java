package com.flipkart.perf.server.cache;

import com.flipkart.perf.server.config.ResourceStorageFSConfig;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 9:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class LibCache {
    private Map<String,String> classLibMap;
    private static LibCache self;
    private ResourceStorageFSConfig storageConfig;

    private String platformZipPath;

    private LibCache(ResourceStorageFSConfig storageConfig) throws IOException {
        this.storageConfig = storageConfig;
        this.classLibMap = new HashMap<String, String>();
        refreshClassLibMap();
        refreshPlatformLibPath();
    }

    public void refreshPlatformLibPath() {
        File platformLibPath = new File(storageConfig.getPlatformLibPath());
        File[] files = platformLibPath.listFiles();
        for(File file :files)
            if(file.getAbsolutePath().endsWith("zip")) {
                this.platformZipPath = file.getAbsolutePath();
                break;
            }
    }

    public String getPlatformZipPath() {
        return platformZipPath;
    }

    public static LibCache initialize(ResourceStorageFSConfig storageConfig) throws IOException {
        self = new LibCache(storageConfig);
        return self;
    }

    public static LibCache instance() {
        return self;
    }

    public void refreshClassLibMap() throws IOException {
        synchronized (classLibMap) {
            classLibMap.clear();

            Properties prop = new Properties();
            InputStream mappingFileIS = new FileInputStream(storageConfig.getUserClassLibMappingFile());
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

    public Map<String,List<String>> getLibsMapWithLibAsKey() {
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

    public Map<String,String> getLibsMapWithClassAsKey() {
        return classLibMap;
    }

    public List<String> classesExist(String[] classes) {
        List<String> classesNotDeployed = new ArrayList<String>();
        for(String className : classes)
            if(!classLibMap.containsKey(className))
                classesNotDeployed.add(className);
        return classesNotDeployed;
    }
}
