package perf.operation.common.data;

import com.open.perf.core.FunctionContext;
import com.open.perf.function.FunctionParameter;
import com.open.perf.function.PerformanceFunction;
import com.open.perf.util.JsonHelper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class JsonDataParser extends PerformanceFunction{
    public static final String IP_JSON_SOURCE = "jsonSource";
    public static final String IP_KEYS_TO_EXTRACT = "keysToExtract";

    @Override
    public void execute(FunctionContext context) throws Exception {
        List<String> keysToExtract = context.getParameterAsList(IP_KEYS_TO_EXTRACT);
        for(String key : keysToExtract)
            context.addParameter(key, JsonHelper.get(context.getParameterAsString("jsonSource"), key));
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
                        setDefaultValue("[]").
                        setDescription("List of keys to be extracted"));

        return parameters;
    }
}
