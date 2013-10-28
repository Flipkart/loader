package perf.operation.redis.function;

import com.flipkart.perf.core.FunctionContext;
import com.flipkart.perf.function.FunctionParameter;
import com.flipkart.perf.function.PerformanceFunction;
import perf.operation.redis.Constants;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class SetPerformanceFunction extends PerformanceFunction{
    private Jedis jedis;
    @Override
    public void init(FunctionContext context) throws Exception{
        jedis = new Jedis(context.getParameterAsString(Constants.IP_REDIS_HOST),
                context.getParameterAsInteger(Constants.IP_REDIS_PORT));

    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        jedis.set(context.getParameterAsString(Constants.IP_KEY),
                context.getParameterAsString(Constants.IP_VALUE));
    }

    @Override
    public void end(FunctionContext context) throws Exception{
        jedis.quit();
    }

    @Override
    public List<String> description() {
        return Arrays.asList(new String[]{"Does Basic Set Function"});
    }

    @Override
    public LinkedHashMap<String, FunctionParameter> inputParameters(){
        LinkedHashMap<String, FunctionParameter> parameters = new LinkedHashMap<String, FunctionParameter>();
        parameters.put(Constants.IP_REDIS_HOST,
                new FunctionParameter().
                        setName(Constants.IP_REDIS_HOST).
                        setDefaultValue("localhost").
                        setDescription("Redis Host/IP").
                        setMandatory(true));

        parameters.put(Constants.IP_REDIS_PORT,
                new FunctionParameter().
                        setName(Constants.IP_REDIS_PORT).
                        setDefaultValue(6379).
                        setDescription("Redis port").
                        setMandatory(true));

        parameters.put(Constants.IP_KEY,
                new FunctionParameter().
                        setName(Constants.IP_KEY).
                        setDescription("Key To Set").
                        setMandatory(true));

        parameters.put(Constants.IP_VALUE,
                new FunctionParameter().
                        setName(Constants.IP_VALUE).
                        setDescription("Value for the key").
                        setMandatory(true));

        return parameters;
    }

}
