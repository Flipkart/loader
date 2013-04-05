package perf.operation.common;

import com.open.perf.core.FunctionContext;
import com.open.perf.function.FunctionParameter;
import com.open.perf.function.PerformanceFunction;

import java.util.LinkedHashMap;

/**
 * Can be used to introduce Delay in the Group
 */
public class DelayFunction extends PerformanceFunction {
    public static final String IP_DELAY = "DELAY";

    @Override
    public void execute(FunctionContext context) throws Exception {
        Thread.sleep(context.getParameterAsInteger(IP_DELAY));
    }

    @Override
    public LinkedHashMap<String, FunctionParameter> inputParameters() {
        LinkedHashMap<String, FunctionParameter> parameters = new LinkedHashMap<String, FunctionParameter>();
        parameters.put(IP_DELAY, new FunctionParameter().
                setName(IP_DELAY).
                setDefaultValue(1).
                setMandatory(true).
                setDescription("Introduces millisecond Delay in function call"));
        return parameters;
    }
}
