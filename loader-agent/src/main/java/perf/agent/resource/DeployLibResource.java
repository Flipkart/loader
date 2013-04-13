package perf.agent.resource;

import com.open.perf.util.FileHelper;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.agent.cache.LibCache;
import perf.agent.config.LibStorageConfig;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/12/12
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/libs")

public class DeployLibResource {
    private static Logger logger = LoggerFactory.getLogger(DeployLibResource.class);
    private LibStorageConfig storageConfig;
    private LibCache libCache;

    public DeployLibResource(LibStorageConfig storageConfig) {
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
        http://localhost:8888/loader-agent/libs/classLibs
     *
     * @param libInputStream jar input stream
     * @param libFileDetails Lib file meta details
     * @param classListStr file containing class names, one in every line
     * @throws IOException
     */
    @Path("/classLibs")
    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    synchronized public void deployLib(
            @FormDataParam("lib") InputStream libInputStream,
            @FormDataParam("lib") FormDataContentDisposition libFileDetails,
            @FormDataParam("classList") String classListStr) throws IOException {

        FileHelper.persistStream(libInputStream, storageConfig.getLibPath()
                + File.separator
                + libFileDetails.getFileName());

        mergeMappingFile(storageConfig.getLibPath()
                + File.separator
                + libFileDetails.getFileName(),
                classListStr,
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
     * @throws IOException
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
     *
     * @param classes comma Separated Classes
     * @return return list of classes for which there are no libs deployed on agent
     * @throws IOException
     */
    @Path("/classLibs/exist")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List searchLibs(@QueryParam("classes") @DefaultValue("") String classes) throws IOException {
        return libCache.classesExist(classes.split(","));
    }


    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl
        -X POST
        -H "Content-Type: multipart/form-data"
        -F "lib=@Path-To-Zip-File-Containing-Platform-Lib-File"
        http://localhost:8888/loader-agent/deploy/platformLibs

     * @param libInputStream zip containing platform jars
     */
    @Path("/platformLibs")
    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    synchronized public String deployPlatformLib(
            @FormDataParam("lib") InputStream libInputStream){

        FileHelper.rename(storageConfig.getPlatformLibPath(), storageConfig.getPlatformLibPath()+".tmp");
        try {
            FileHelper.unzip(libInputStream, storageConfig.getPlatformLibPath());
            FileHelper.remove(storageConfig.getPlatformLibPath() + ".tmp");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            FileHelper.remove(storageConfig.getPlatformLibPath());
            FileHelper.rename(storageConfig.getPlatformLibPath() + ".tmp", storageConfig.getPlatformLibPath());
        } finally {
            this.libCache.refreshPlatformLib();
        }
        return "Successful Deployment";
    }

    @Path("/platformLibs")
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    synchronized public List getPlatformLib(){
        return this.libCache.getPlatformLibs();
    }

    synchronized public static void mergeMappingFile(String libPath, String classListStr, String mappingFile) throws IOException {
        InputStream mappingFileIS = new FileInputStream(mappingFile);
        Properties prop = new Properties();
        FileHelper.createFile(mappingFile);
        prop.load(mappingFileIS);

        String[] classes = classListStr.split("\n");
        for(String className : classes) {
            if(!className.trim().equals(""))
                prop.put(className, libPath);
        }
        prop.store(new FileOutputStream(mappingFile), "Class and Library Mapping");
        mappingFileIS.close();
    }


}