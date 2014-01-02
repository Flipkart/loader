package com.flipkart.perf.function;

import com.flipkart.perf.core.FunctionContext;
import com.flipkart.perf.inmemorydata.SharedDataInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

abstract public class PerformanceFunction {
    protected static Logger logger = LoggerFactory.getLogger(PerformanceFunction.class);

    /**
     * Will Be called only once. Before the actual run starts.
     * See it as an initialization area
     * You could set any variables that would need when the execute function is called.
     * @param context
     * @throws Exception
     */
    public void init(FunctionContext context) throws Exception{
        logger.debug("In Default PerformanceFunction Init");
    }

    /**
     * Logic that user wants to execute as part of performance run
     * @param context
     * @throws Exception
     */
    abstract public void execute(FunctionContext context) throws Exception;

    /**
     * Will Be called only once. After Run is over.
     * See it as a clean up section to release resources
     * @param context
     * @throws Exception
     */
    public void end(FunctionContext context) throws Exception{
        logger.debug("In Default PerformanceFunction end");
    }

    /**
     * Can be used by user to get some information about the functionality of this class
     * @return
     */
    public List<String> description() {
        return Arrays.asList(new String[]{"Default Description : Its a Performance Function"});
    }

    /**
     * Can be used by user to explicitly find out input parameters for a function
     * @return
     */
    public LinkedHashMap<String, FunctionParameter> inputParameters(){
        return new LinkedHashMap<String, FunctionParameter>();
    }

    /**
     * Can be used by user to explicitly find out output parameters for a function
     * @return
     */
    public LinkedHashMap<String, FunctionParameter> outputParameters(){
        return new LinkedHashMap<String, FunctionParameter>();
    }

    public List<String> customCounters() {
        return new ArrayList<String>();
    }

    public List<String> customHistograms() {
        return new ArrayList<String>();
    }

    public List<String> customTimers() {
        return new ArrayList<String>();
    }
    /**
     * Can be used by user to explicitly tell framework about the shared structures its going to use
     * @return
     */
    public LinkedHashMap<String, SharedDataInfo> sharedData(){
        return new LinkedHashMap<String, SharedDataInfo>();
    }
}