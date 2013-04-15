package perf.operation.http.function;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.open.perf.core.FunctionContext;
import com.open.perf.function.FunctionParameter;
import com.open.perf.function.PerformanceFunction;
import perf.operation.http.constant.Constants;
import perf.operation.http.util.HttpRequestHelper;
import perf.operation.http.util.HttpResponseHelper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
/**
 * Does Http Get Operation
 */
/*
    Example docs to follow :
    - http://jfarcand.wordpress.com/2010/12/21/going-asynchronous-using-asynchttpclient-the-basic/
 */
public class HttpGet extends PerformanceFunction implements Constants {
    private AsyncHttpClient asyncHttpClient;

    @Override
    public void init(FunctionContext context) {
        this.asyncHttpClient = HttpRequestHelper.buildClient();
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.
                prepareGet(context.getParameterAsString(IP_PARAM_URL));

        HttpRequestHelper.enhanceRequest(context, requestBuilder);

        Response response = HttpRequestHelper.executeRequest(context, requestBuilder);

        if(HttpResponseHelper.successfulRequest(context, response)) {
            HttpResponseHelper.passOnResponse(context, response);
        }
    }

    @Override
    public void end(FunctionContext context) {
        logger.info("Closing Connection");
        this.asyncHttpClient.close();
    }

    @Override
    public LinkedHashMap<String, FunctionParameter> inputParameters() {
        LinkedHashMap<String, FunctionParameter> parameters = new LinkedHashMap<String, FunctionParameter>();
        HttpRequestHelper.addInputUrlParam(parameters);
        HttpRequestHelper.addInputExpectedStatusCode(parameters);
        HttpRequestHelper.addInputFollowRedirects(parameters);
        HttpRequestHelper.addInputHeaders(parameters);
        HttpRequestHelper.addInputParameters(parameters);
        HttpRequestHelper.addInputQueryParameters(parameters);
        HttpRequestHelper.addInputProxy(parameters);
        HttpRequestHelper.addInputCookies(parameters);
        return parameters;
    }

    @Override
    public List<String> description() {
        return Arrays.asList(new String[]{
                "This Performance function is useful in doing Http Get Operation",
                "Time out for request is 120seconds"
        });
    }
}
