package perf.server.resource;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.metrics.annotation.Timed;
import org.apache.log4j.Logger;
import perf.server.cache.LibCache;
import perf.server.config.LibStorageFSConfig;
import perf.server.util.FileHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Path("/libs")

public class DeployLibResource {
    private static Logger log = Logger.getLogger(DeployLibResource.class);
    private LibStorageFSConfig storageConfig;
    private LibCache libCache;

    public DeployLibResource(LibStorageFSConfig storageConfig) {
        this.storageConfig = storageConfig;
        this.libCache = LibCache.getInstance();
    }

    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl
        -X POST
        -H "Content-Type: multipart/form-data"
        -F "lib=@Path-To-Jar-File"
        -F "classList=@Path-To-File-Containing-Class-Names-Separated-With-New-Line"
        http://localhost:8888/loader-server/libs/classLibs
     *
     * @param libInputStream jar input stream
     * @param libFileDetails Lib file meta details
     * @param classListInputStream file containing class names, one in every line
     * @throws java.io.IOException
     */
    @Path("/classLibs")
    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    synchronized public void deployLib(
            @FormDataParam("lib") InputStream libInputStream,
            @FormDataParam("lib") FormDataContentDisposition libFileDetails,
            @FormDataParam("classList") InputStream classListInputStream) throws IOException {

        FileHelper.persistStream(libInputStream, storageConfig.getLibPath()
                + File.separator
                + libFileDetails.getFileName());

        FileHelper.mergeMappingFile(storageConfig.getLibPath()
                + File.separator
                + libFileDetails.getFileName(),
                classListInputStream,
                storageConfig.getMappingFile());

        this.libCache.refreshClassLibMap();
    }

    enum MapKey {
        LIB,CLASS;
    }


    /**
     *
     * @param mapKey takes LIB or CLASS as value. Default value is LIB
     * @return returns either Map(lib -> list of class) or Map(class -> Lib) depending upon mapKey
     * @throws java.io.IOException
     */
    @Path("/classLibs")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map getLibs(@QueryParam("mapKey") @DefaultValue("LIB") String mapKey) throws IOException {

        switch(MapKey.valueOf(mapKey)) {
            case LIB:
                return libCache.getLibsMapWithLibAsKey();
            case CLASS:
                return libCache.getLibsMapWithClassAsKey();
            default:
                throw new WebApplicationException(400);
        }
    }


    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl
        -X POST
        -H "Content-Type: multipart/form-data"
        -F "lib=@Path-To-Zip-File-Containing-Platform-Lib-File"
        http://localhost:8888/loader-server/libs/platformLibs

     * @param libInputStream zip containing platform jars
     */
    @Path("/platformLibs")
    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    synchronized public void deployPlatformLib(
            @FormDataParam("lib") InputStream libInputStream){
        String platformZipPath = storageConfig.getPlatformLibPath()+ File.separator + "platform.zip";
        String tmpPlatformZipPath = storageConfig.getPlatformLibPath()+ File.separator + "platform.zip.tmp";

        try {
            FileHelper.move(platformZipPath,tmpPlatformZipPath);
            FileHelper.persistStream(libInputStream, platformZipPath);
            FileHelper.unzip(new FileInputStream(platformZipPath), storageConfig.getPlatformLibPath());
            FileHelper.remove(tmpPlatformZipPath);

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            FileHelper.move(tmpPlatformZipPath, platformZipPath);
        }
        finally {
            this.libCache.refreshPlatformLibPath();
        }
    }
}