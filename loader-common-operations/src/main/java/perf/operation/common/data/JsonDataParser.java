package perf.operation.common.data;

import com.flipkart.perf.core.FunctionContext;
import com.flipkart.perf.function.FunctionParameter;
import com.flipkart.perf.function.PerformanceFunction;
import com.flipkart.perf.common.util.JsonHelper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonDataParser extends PerformanceFunction{
    public static final String IP_JSON_SOURCE = "jsonSource";
    public static final String IP_KEYS_TO_EXTRACT = "keysToExtract";

    @Override
    public void execute(FunctionContext context) throws Exception {
        Map keysToExtract = context.getParameterAsMap(IP_KEYS_TO_EXTRACT);
        for(Object key : keysToExtract.keySet())
            context.addParameter(keysToExtract.get(key).toString(),
                    JsonHelper.get(context.getParameterAsString("jsonSource"), key.toString()));
    }

    @Override
    public List<String> description() {
        return Arrays.asList(new String[]{
                "This Function is useful to extract fields from incoming json field jsonData",
                "Direct json key can be used to extract top level json values",
                " - You can use '.' between fields to extract sub fields in jsonData",
                " [] can be used to extract array values",
                "Possible use case is to string this function after some http call which returns a json response"
        });
    }

    @Override
    public LinkedHashMap<String, FunctionParameter> inputParameters(){
        LinkedHashMap<String, FunctionParameter> parameters = new LinkedHashMap<String, FunctionParameter>();
        parameters.put(IP_JSON_SOURCE,
                new FunctionParameter().
                        setName(IP_JSON_SOURCE).
                        setMandatory(true).
                        setDefaultValue("").
                        setDescription("Json Source String"));

        parameters.put(IP_KEYS_TO_EXTRACT,
                new FunctionParameter().
                        setName(IP_KEYS_TO_EXTRACT).
                        setMandatory(true).
                        setDefaultValue("{}").
                        setDescription("json representing key (key is nested json keys) " +
                                "and value is the name with which you want to store the extracted values"));

        return parameters;
    }
}
