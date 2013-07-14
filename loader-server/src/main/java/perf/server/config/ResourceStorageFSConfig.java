package perf.server.config;

import java.io.File;

public class ResourceStorageFSConfig {
    private String userClassLibMappingFile;
    private String udfLibsPath;
    private String platformLibPath;
    private String userClassInfoPath;
    private String inputFilesPath;
    private String inputFilePath;

    public String getUdfLibsPath() {
        return udfLibsPath;
    }

    public ResourceStorageFSConfig setUdfLibsPath(String udfLibsPath) {
        this.udfLibsPath = udfLibsPath;
        return this;
    }

    public String getUserClassLibMappingFile() {
        return userClassLibMappingFile;
    }

    public ResourceStorageFSConfig setUserClassLibMappingFile(String userClassLibMappingFile) {
        this.userClassLibMappingFile = userClassLibMappingFile;
        return this;
    }

    public String getPlatformLibPath() {
        return platformLibPath;
    }

    public ResourceStorageFSConfig setPlatformLibPath(String platformLibPath) {
        this.platformLibPath = platformLibPath;
        return this;
    }

    public String getUserClassInfoPath() {
        return userClassInfoPath;
    }

    public String getUserClassInfoFile(String function) {
        return userClassInfoPath + File.separator + function + ".info.json";
    }

    public ResourceStorageFSConfig setUserClassInfoPath(String userClassInfoPath) {
        this.userClassInfoPath = userClassInfoPath;
        return this;
    }

    public String getInputFilesPath() {
        return inputFilesPath;
    }

    public ResourceStorageFSConfig setInputFilesPath(String inputFilesPath) {
        this.inputFilesPath = inputFilesPath;
        return this;
    }

    public String getInputFilePath(String resourceName) {
        return inputFilePath.replace("{resourceName}", resourceName);
    }

    public ResourceStorageFSConfig setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
        return this;
    }
}
