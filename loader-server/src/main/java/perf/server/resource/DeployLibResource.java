package perf.server.resource;

import com.google.common.collect.Multimap;
import com.open.perf.function.FunctionParameter;
import com.open.perf.jackson.ObjectMapperUtil;
import com.open.perf.util.ClassHelper;
import com.open.perf.util.FileHelper;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.metrics.annotation.Timed;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.reflections.Reflections;
import org.reflections.Store;
import perf.server.cache.LibCache;
import perf.server.config.LibStorageFSConfig;
import perf.server.domain.FunctionInfo;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


/**
 * Resource that deploy libs on the server
 */
@Path("/libs")
public class DeployLibResource {
    private static Logger log = Logger.getLogger(DeployLibResource.class);
    private LibStorageFSConfig storageConfig;
    private LibCache libCache;
    private CustomClassLoader customClassLoader;
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();

    public DeployLibResource(LibStorageFSConfig storageConfig) throws MalformedURLException {
        this.storageConfig = storageConfig;
        this.libCache = LibCache.instance();
        loadPlatformLibsInCustomClassLoader();
    }

    // Needed. It allows me to instantiate UDF and extract information
    private void loadPlatformLibsInCustomClassLoader() throws MalformedURLException {
        customClassLoader = null;
        URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        customClassLoader = new CustomClassLoader(loader.getURLs());
        File platformLibPath = new File(this.storageConfig.getPlatformLibPath());
        if(platformLibPath.exists()) {
            File[] platformLibs = platformLibPath.listFiles();
            for(File platformLib : platformLibs) {
                customClassLoader.addURL(new URL("file://" + platformLib.getAbsolutePath()));
            }
        }
    }

    static class CustomClassLoader extends URLClassLoader {
        public CustomClassLoader(URL[] urls) {
            super(urls);
        }

        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }
    }

    /**
     Following call simulates html form post call, where somebody uploads a file to server
     curl
     -X POST
     -H "Content-Type: multipart/form-data"
     -F "lib=@Path-To-Jar-File"
     http://localhost:8888/loader-server/libs/classLibs
     *
     *
     *
     * @param libInputStream jar input stream
     * @param libFileDetails Lib file meta details
     * @throws java.io.IOException
     */
    @Path("/classLibs")
    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    synchronized public Map<String, FunctionInfo> deployLib(
            @FormDataParam("lib") InputStream libInputStream,
            @FormDataParam("lib") FormDataContentDisposition libFileDetails) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        String userLibPath = storageConfig.getUserLibPath()
                + File.separator
                + libFileDetails.getFileName();

        FileHelper.persistStream(libInputStream, userLibPath);

        Map<String, FunctionInfo> discoveredUserFunctions = discoverUserFunctions(userLibPath);

        persistDiscoveredUserFunctions(libFileDetails.getFileName(), discoveredUserFunctions);

        this.libCache.refreshClassLibMap();

        return discoveredUserFunctions;
    }

    /**     * Persist user class and jar mapping
     * Persist Class information which could be later presented vie http get end point or UI
     * @param libFileName
     * @param discoveredUserFunctions
     * @throws IOException
     */
    private void persistDiscoveredUserFunctions(String libFileName, Map<String, FunctionInfo> discoveredUserFunctions) throws IOException {
        for(String userFunction : discoveredUserFunctions.keySet()) {
            mergeMappingFile(storageConfig.getUserLibPath()
                    + File.separator
                    + libFileName,
                    userFunction);

            FunctionInfo functionInfo = discoveredUserFunctions.get(userFunction);
            String functionInfoFile = storageConfig.getUserClassInfoPath() + File.separator + userFunction + ".info.json";
            FileHelper.createFile(functionInfoFile);
            objectMapper.writeValue(new File(functionInfoFile), functionInfo);
        }
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
            loadPlatformLibsInCustomClassLoader();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            FileHelper.move(tmpPlatformZipPath, platformZipPath);
        }
        finally {
            this.libCache.refreshPlatformLibPath();
        }
    }

    @Path("/platformLibs")
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    synchronized public List getPlatformLib(){
        return Arrays.asList(new File(storageConfig.getPlatformLibPath()).list());
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String externalJar = "/home/nitinka/git/loader2.0/loader-http-operations/target/loader-http-operations-1.0-SNAPSHOT-jar-with-dependencies.jar";
        LibStorageFSConfig storageFSConfig = new LibStorageFSConfig().
                setPlatformLibPath("/usr/share/loader-server/platformLibs/").
                setUserClassInfoPath("/usr/share/loader-server/config").
                setUserClassLibMappingFile("/usr/share/loader-server/config/classLibMapping.properties");
        DeployLibResource deploy = new DeployLibResource(storageFSConfig);
        Map<String, FunctionInfo> discoveredUserFunctions = deploy.discoverUserFunctions(externalJar);
        deploy.persistDiscoveredUserFunctions("sample", discoveredUserFunctions);
        System.out.println(discoveredUserFunctions);
    }

    /**
     * Discover Performance Functions from the uploader userLibJar
     * @param userLibJar
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Map<String,FunctionInfo> discoverUserFunctions(String userLibJar) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, FunctionInfo> discoveredUserFunctions = new HashMap<String, FunctionInfo>();

        customClassLoader.addURL(new URL("file://" + userLibJar));
        System.out.println("User Lib Path = " + userLibJar);
        Reflections reflections = new Reflections("");
        reflections.scan(new URL("file://"+userLibJar));

        Store reflectionStore = reflections.getStore();
        Map<String, Multimap<String, String>> storeMap = reflectionStore.getStoreMap();
        Multimap<String,String> subTypesScanner = storeMap.get("SubTypesScanner");

        if(subTypesScanner != null) {
            Collection<String> performanceFunctions = subTypesScanner.get("com.open.perf.function.PerformanceFunction");
            for(String performanceFunction : performanceFunctions) {
                if(!discoveredUserFunctions.containsKey(performanceFunction)) {
                    FunctionInfo functionInfo = new FunctionInfo().
                            setFunction(performanceFunction);
                    // Discover Usage description for the UDF
                    Object object = ClassHelper.getClassInstance(performanceFunction, new Class[]{}, new Object[]{}, customClassLoader);
                    Method method = ClassHelper.getMethod(performanceFunction , "description", new Class[]{}, customClassLoader);
                    functionInfo.setDescription((List<String>) method.invoke(object, new Object[]{}));

                    // Discover Input parameters for the UDF
                    method = ClassHelper.getMethod(performanceFunction , "inputParameters", new Class[]{}, customClassLoader);
                    functionInfo.setInputParameters((LinkedHashMap<String, FunctionParameter>) method.invoke(object, new Object[]{}));

                    // Discover Output parameters for the UDF
                    method = ClassHelper.getMethod(performanceFunction , "outputParameters", new Class[]{}, customClassLoader);
                    functionInfo.setOutputParameters((LinkedHashMap<String, FunctionParameter>) method.invoke(object, new Object[]{}));

                    discoveredUserFunctions.put(performanceFunction, functionInfo);
                }
           }
        }
        return discoveredUserFunctions;
    }

    /**
     * Update Mapping file which has map of User Function Class and Jar containing that class
     * @param libPath
     * @param userFunctionClass
     * @throws IOException
     */
    synchronized private void mergeMappingFile(String libPath, String userFunctionClass) throws IOException {
        String mappingFile = storageConfig.getUserClassLibMappingFile();

        Properties prop = new Properties();
        FileHelper.createFile(mappingFile);
        InputStream mappingFileIS = new FileInputStream(mappingFile);
        try {
            FileHelper.createFile(mappingFile);
            prop.load(mappingFileIS);
            prop.put(userFunctionClass, libPath);
            prop.store(new FileOutputStream(mappingFile), "Class and Library Mapping");
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            mappingFileIS.close();
        }
    }
}