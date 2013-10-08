package com.flipkart.perf.server.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.flipkart.perf.domain.Group;
import com.flipkart.perf.domain.GroupFunction;
import com.flipkart.perf.domain.Load;
import com.flipkart.perf.function.FunctionParameter;
import org.codehaus.jackson.map.ObjectMapper;

import com.flipkart.perf.server.config.ResourceStorageFSConfig;
import com.flipkart.perf.server.domain.FunctionInfo;
import com.flipkart.perf.server.domain.LoadPart;
import com.flipkart.perf.server.domain.MetricCollection;
import com.flipkart.perf.server.domain.OnDemandMetricCollection;
import com.flipkart.perf.server.domain.PerformanceRun;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import com.flipkart.perf.server.util.ResponseBuilder;

import com.yammer.dropwizard.jersey.params.BooleanParam;


/**
 * Resource can be used to search available functions that can be used for load generation
 */
@Path("/functions")
public class FunctionResource {
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();
    private ResourceStorageFSConfig storageConfig;

    public FunctionResource(ResourceStorageFSConfig storageConfig) throws MalformedURLException {
        this.storageConfig = storageConfig;
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
                if(userFunctionFileName.endsWith("info.json")) {
                    String function = userFunctionFileName.replace(".info.json","");
                    if(Pattern.matches(functionsRegEx, function)) {
                        if(includeClassInfo.get()) {
                            FunctionInfo functionInfo = objectMapper.readValue(userFunctionFile, FunctionInfo.class);
                            userFunctions.add(functionInfo);
                        }
                        else {
                            userFunctions.add(function);
                        }
                    }
                }
            }
        }
        return userFunctions;
    }

    /**
     * Get All Deployed Functions
     * @return
     * @throws IOException
     */
    @Path("/{function}/performanceRun")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PerformanceRun getFunctions(@PathParam("function") String function,
                                     @QueryParam("runName") String runName) throws IOException {

        File userFunctionsInfoFile = new File(storageConfig.getUserClassInfoFile(function));
        if(!userFunctionsInfoFile.exists())
            throw new WebApplicationException(ResponseBuilder.resourceNotFound("Function", function));

        String functionClassName = function.split("\\.")[function.split("\\.").length-1];
        if(runName == null)
            runName = "defaultRun_" + function;

        FunctionInfo functionInfo = objectMapper.readValue(userFunctionsInfoFile, FunctionInfo.class);

        GroupFunction groupFunction = new GroupFunction().
                setDumpData(true).
                setFunctionalityName(functionClassName + "_with_" + function).setFunctionClass(function);
        Map<String, FunctionParameter> functionInputParameters = functionInfo.getInputParameters();
        for(String inputParameterName : functionInputParameters.keySet()) {
            FunctionParameter inputParameterInfo = functionInputParameters.get(inputParameterName);
            groupFunction.addParam(inputParameterName, inputParameterInfo.getDefaultValue());
        }

        Group group = new Group().
                setName("defaultGroup_" + functionClassName + "_" + function).
                setFunctions(Arrays.asList(new GroupFunction[]{groupFunction}));

        Load load = new Load().
                setGroups(Arrays.asList(new Group[]{group}));

        LoadPart loadPart = new LoadPart().
                setName("default").
                setAgents(1).
                setClasses(Arrays.asList(new String[]{function})).
                setLoad(load);

        return new PerformanceRun().
                setRunName(runName).
                setMetricCollections(Arrays.asList(new MetricCollection[]{})).
                setOnDemandMetricCollections(Arrays.asList(new OnDemandMetricCollection[]{})).
                setLoadParts(Arrays.asList(new LoadPart[]{loadPart}));
    }


    /**
     * Get All Deployed Functions
     * @return
     * @throws IOException
     */
    @Path("/{functionsRegEx}")
    @DELETE
    public void deleteFunctions(@PathParam("functionsRegEx") @DefaultValue("$%^&*") String functionsRegEx) throws IOException {
        functionsRegEx = ".*" + functionsRegEx + ".*";
        System.out.println("functionsRegEx: "+functionsRegEx);
        File userFunctionsBaseFolder = new File(storageConfig.getUserClassInfoPath());
        if(userFunctionsBaseFolder.exists()) {
            Properties mappingProp = new Properties();
            mappingProp.load(new FileInputStream(storageConfig.getUserClassLibMappingFile()));
            for(File userFunctionFile : userFunctionsBaseFolder.listFiles()){
                String userFunctionFileName = userFunctionFile.getName();
                if(userFunctionFileName.endsWith("info")) {
                    userFunctionFileName = userFunctionFileName.replace(".info.json","");
                    if(Pattern.matches(functionsRegEx, userFunctionFileName)) {
                        userFunctionFile.delete();
                        mappingProp.remove(userFunctionFileName);
                    }
                }
            }
            mappingProp.store(new FileOutputStream(storageConfig.getUserClassLibMappingFile()), "Class and Library Mapping");
        }
    }

    public static void main(String[] args) {
        System.out.println("H.L".split("\\.").length);
    }
}
