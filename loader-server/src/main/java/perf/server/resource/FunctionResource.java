package perf.server.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.dropwizard.jersey.params.BooleanParam;
import org.apache.log4j.Logger;
import perf.server.config.LibStorageFSConfig;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Resource can be used to search available functions that can be used for load generation
 */
@Path("/functions")
public class FunctionResource {
    private static Logger log = Logger.getLogger(FunctionResource.class);
    private LibStorageFSConfig storageConfig;
    private ObjectMapper objectMapper;

    public FunctionResource(LibStorageFSConfig storageConfig) throws MalformedURLException {
        this.storageConfig = storageConfig;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get All Deployed Functions
     * @return
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Object> getFunctions(@QueryParam("classInfo") @DefaultValue("false") BooleanParam includeClassInfo) throws IOException {
        return getFunctions("",includeClassInfo);
    }

    /**
     * Get All Deployed Functions
     * @return
     * @throws IOException
     */
    @Path("/{functionsRegEx}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Object> getFunctions(@PathParam("functionsRegEx") @DefaultValue(".+") String functionsRegEx,
                                     @QueryParam("classInfo") @DefaultValue("false") BooleanParam includeClassInfo) throws IOException {

        functionsRegEx = ".*" + functionsRegEx + ".*";
        System.out.println("functionsRegEx: "+functionsRegEx);
        List<Object> userFunctions = new ArrayList<Object>();
        File userFunctionsBaseFolder = new File(storageConfig.getUserClassInfoPath());
        if(userFunctionsBaseFolder.exists()) {
            for(File userFunctionFile : userFunctionsBaseFolder.listFiles()){
                String userFunctionFileName = userFunctionFile.getName();
                System.out.println("userFunctionFileName: "+userFunctionFileName);
                if(userFunctionFileName.endsWith("info")) {
                    userFunctionFileName = userFunctionFileName.replace(".info","");
                    if(Pattern.matches(functionsRegEx, userFunctionFileName)) {
                        if(includeClassInfo.get()) {
                            Map<String, LinkedHashMap> classInfoMap = new HashMap<String, LinkedHashMap>();
                            LinkedHashMap classInfo = new LinkedHashMap();
                            Properties prop = new Properties();
                            prop.load(new FileInputStream(userFunctionFile));
                            Enumeration classProperties = prop.propertyNames();
                            while(classProperties.hasMoreElements()) {
                                Object property = classProperties.nextElement();
                                Object propertyValue = prop.get(property);
                                classInfo.put(property.toString(), objectMapper.readValue(propertyValue.toString(),LinkedHashMap.class));
                            }
                            classInfoMap.put(userFunctionFileName, classInfo);
                            userFunctions.add(classInfoMap);
                        }
                        else {
                            userFunctions.add(userFunctionFileName.replace(".info",""));
                        }
                    }
                }
            }
        }
        return userFunctions;
    }
}