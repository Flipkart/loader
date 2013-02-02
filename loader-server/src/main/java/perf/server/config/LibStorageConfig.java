package perf.server.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 7:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class LibStorageConfig {
    private String mappingFile;
    private String libPath;
    private String platformLibPath;

    public String getLibPath() {
        return libPath;
    }

    public void setLibPath(String libPath) {
        this.libPath = libPath;
    }

    public String getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(String mappingFile) {
        this.mappingFile = mappingFile;
    }

    public String getPlatformLibPath() {
        return platformLibPath;
    }

    public void setPlatformLibPath(String platformLibPath) {
        this.platformLibPath = platformLibPath;
    }
}
