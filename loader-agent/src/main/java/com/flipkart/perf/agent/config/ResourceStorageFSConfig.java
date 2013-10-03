package com.flipkart.perf.agent.config;

public class ResourceStorageFSConfig {
    private String mappingFile;
    private String udfLibsPath;
    private String platformLibPath;
    private String inputFilePath;

    public String getUdfLibsPath() {
        return udfLibsPath;
    }

    public ResourceStorageFSConfig setUdfLibsPath(String udfLibsPath) {
        this.udfLibsPath = udfLibsPath;
        return this;
    }

    public String getMappingFile() {
        return mappingFile;
    }

    public ResourceStorageFSConfig setMappingFile(String mappingFile) {
        this.mappingFile = mappingFile;
        return this;
    }

    public String getPlatformLibPath() {
        return platformLibPath;
    }

    public ResourceStorageFSConfig setPlatformLibPath(String platformLibPath) {
        this.platformLibPath = platformLibPath;
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
