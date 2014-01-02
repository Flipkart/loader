package perf.operation.http.function;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.flipkart.perf.core.FunctionContext;
import com.flipkart.perf.function.FunctionParameter;
import com.flipkart.perf.function.PerformanceFunction;
import perf.operation.http.constant.Constants;
import perf.operation.http.util.HttpRequestHelper;
import perf.operation.http.util.HttpResponseHelper;

import java.lang.Override;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Does Http Post Operation
 */
public class HttpPost extends PerformanceFunction implements Constants {
    private AsyncHttpClient asyncHttpClient;

    @Override
    public void init(FunctionContext context) {
        this.asyncHttpClient = HttpRequestHelper.buildClient();
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.
                preparePost(context.getParameterAsString(IP_PARAM_URL));

        HttpRequestHelper.enhanceRequest(context, requestBuilder);
        HttpRequestHelper.addRequestBody(context, requestBuilder);
        HttpRequestHelper.setCharacterEncoding(context, requestBuilder);

        Response response = HttpRequestHelper.executeRequest(context, requestBuilder);

        if(HttpResponseHelper.successfulRequest(context, response)) {
            context.updateHistogram("body-size", response.getResponseBody().length());
            HttpResponseHelper.passOnResponse(context, response);
        }
    }

    @Override
    public void end(FunctionContext context) {
        logger.info("Closing Connection");
        HttpRequestHelper.closeConnection();
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
        HttpRequestHelper.addInputBodyString(parameters);
        HttpRequestHelper.addInputBodyFile(parameters);
        HttpRequestHelper.addInputCharacterEncoding(parameters);
        HttpRequestHelper.addInputPassOnBody(parameters);
        return parameters;
    }

    @Override
    public List<String> description() {
        return Arrays.asList(new String[]{
                "This Performance function is useful in doing Http Post Operation",
                "Time out for request is 120seconds"
        });
    }

    @Override
    public List<String> customHistograms() {
        return Arrays.asList(new String[]{"body-size"});
    }

}

