package com.open.perf.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 14/7/13
 * Time: 8:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class FSConfig {
    public static final String INPUT_FILE_PATH_TEMPLATE = "/usr/share/loader-agent/inputFiles/{resourceName}/inputFile";
    public static final String INPUT_FILES_PATH = "/usr/share/loader-agent/inputFiles";
    public static String getInputFilePath(String resourceName) {
        return INPUT_FILE_PATH_TEMPLATE.replace("{resourceName}", resourceName);
    }

    /**
     * Get map of resource name and input file Resource path
     * @return
     */
    public static Map<String, String> inputFileResources() {
        Map<String, String> fileResources = new HashMap<String, String>();
        File inputFilesPath = new File(INPUT_FILES_PATH);
        if(inputFilesPath.exists()) {
            for(File inputFilePath : inputFilesPath.listFiles()) {
                fileResources.put(inputFilePath.getName(), getInputFilePath(inputFilePath.getName()));
            }
        }
        return fileResources;
    }
}
