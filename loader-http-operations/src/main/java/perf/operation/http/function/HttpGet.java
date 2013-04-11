package perf.operation.http.function;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.open.perf.core.FunctionContext;
import com.open.perf.function.FunctionParameter;
import com.open.perf.function.PerformanceFunction;
import perf.operation.http.constant.Constants;
import perf.operation.http.util.HttpRequestHelper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Future;

import static com.open.perf.core.FunctionContext.FailureType.FUNCTIONAL_FAILURE;
/**
 * Does Http Gen Operations
 */
/*
    Example docs to follow :
    - http://jfarcand.wordpress.com/2010/12/21/going-asynchronous-using-asynchttpclient-the-basic/
 */
public class HttpGet extends PerformanceFunction implements Constants {
    private AsyncHttpClient asyncHttpClient;

    @Override
    public void init(FunctionContext context) {
        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setAllowPoolingConnection(true).
                setMaximumConnectionsTotal(1000).
                setCompressionEnabled(true).
                setRequestTimeoutInMs(120000);
        logger.info("Creating Connection");
        this.asyncHttpClient = new AsyncHttpClient(builder.build());
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        System.out.println("Url: "+context.getParameterAsString(IP_PARAM_URL));
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.
                prepareGet(context.getParameterAsString(IP_PARAM_URL));

        HttpRequestHelper.addHeaders(context, requestBuilder);
        HttpRequestHelper.addQueryParameters(context, requestBuilder);
        HttpRequestHelper.addParameters(context, requestBuilder);
        HttpRequestHelper.addProxy(context, requestBuilder);
        HttpRequestHelper.setFollowRedirects(context, requestBuilder);
        HttpRequestHelper.addCookies(context, requestBuilder);

        context.startMe();
        Future<Response> responseF = requestBuilder.execute();
        Response response = responseF.get();
        context.endMe();

        if(response.getStatusCode() != context.getParameterAsInteger(IP_EXPECTED_STATUS_CODE)) {
            context.failed(FUNCTIONAL_FAILURE,"Status code "+response.getStatusCode());
        } else {
            if(context.getParameterAsBoolean(IP_PASS_ON_BODY))
                context.addParameter(OP_HTTP_RESPONSE, response.getResponseBody());
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
        parameters.put(IP_PARAM_URL,
                new FunctionParameter().
                        setName(IP_PARAM_URL).
                        setMandatory(true).
                        setDefaultValue("").
                        setDescription("Url to do http Get"));
        parameters.put(IP_EXPECTED_STATUS_CODE,
                        new FunctionParameter().
                                setName(IP_EXPECTED_STATUS_CODE).
                                setMandatory(true).
                                setDefaultValue(200).
                                setDescription("Status code to validate"));
        parameters.put(IP_FOLLOW_REDIRECTS,
                        new FunctionParameter().
                                setName(IP_FOLLOW_REDIRECTS).
                                setMandatory(false).
                                setDefaultValue(false).
                                setDescription("If you want to allow request redirects"));
        parameters.put(IP_HEADERS,
                        new FunctionParameter().
                                setName(IP_HEADERS).
                                setMandatory(false).
                                setDefaultValue("{}").
                                setDescription("Http Request Headers as json. Eg: {'content-type' : 'application/json', 'accept' : 'application/json'}"));
        parameters.put(IP_PARAMETERS,
                        new FunctionParameter().
                                setName(IP_PARAMETERS).
                                setMandatory(false).
                                setDefaultValue("{}").
                                setDescription("Http Request Parameters as json"));
        parameters.put(IP_QUERY_PARAMETERS,
                        new FunctionParameter().
                                setName(IP_QUERY_PARAMETERS).
                                setMandatory(false).
                                setDefaultValue("{}").
                                setDescription("Http Request Query Parameters as json"));

        parameters.put(IP_PROXY,
                        new FunctionParameter().
                                setName(IP_PROXY).
                                setMandatory(false).
                                setDefaultValue("{}").
                                setDescription("Proxy for the request as json. Eg : {'host' : '127.0.0.1', 'port' : '1234'}"));

        parameters.put(IP_COOKIES,
                        new FunctionParameter().
                                setName(IP_COOKIES).
                                setMandatory(false).
                                setDefaultValue("[]").
                                setDescription("Proxy for the request as json. Eg : [{'name' : 'value'}]"));
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

