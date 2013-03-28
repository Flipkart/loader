package perf.server.config;

public class LibStorageFSConfig {
    private String userClassLibMappingFile;
    private String userLibPath;
    private String platformLibPath;
    private String userClassInfoPath;

    public String getUserLibPath() {
        return userLibPath;
    }

    public LibStorageFSConfig setUserLibPath(String userLibPath) {
        this.userLibPath = userLibPath;
        return this;
    }

    public String getUserClassLibMappingFile() {
        return userClassLibMappingFile;
    }

    public LibStorageFSConfig setUserClassLibMappingFile(String userClassLibMappingFile) {
        this.userClassLibMappingFile = userClassLibMappingFile;
        return this;
    }

    public String getPlatformLibPath() {
        return platformLibPath;
    }

    public LibStorageFSConfig setPlatformLibPath(String platformLibPath) {
        this.platformLibPath = platformLibPath;
        return this;
    }

    public String getUserClassInfoPath() {
        return userClassInfoPath;
    }

    public LibStorageFSConfig setUserClassInfoPath(String userClassInfoPath) {
        this.userClassInfoPath = userClassInfoPath;
        return this;
    }
}
