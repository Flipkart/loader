package perf.operation.http;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.open.perf.core.FunctionContext;
import com.open.perf.function.FunctionParameter;
import com.open.perf.function.PerformanceFunction;

import java.util.LinkedHashMap;
import java.util.concurrent.Future;

import static com.open.perf.core.FunctionContext.FailureType.FUNCTIONAL_FAILURE;
/**
 * Does Http Gen Operations
 */
/*
    Example docs to follow :
    - http://jfarcand.wordpress.com/2010/12/21/going-asynchronous-using-asynchttpclient-the-basic/
 */
public class HttpGet extends PerformanceFunction {
    public static final String IP_PARAM_URL = "url";
    public static final String IP_EXPECTED_STATUS_CODE = "expectedStatusCode";
    public static final String IP_PASS_ON_BODY = "passOnBody";

    private static final String OP_HTTP_RESPONSE = "httpResponse";
    private AsyncHttpClient asyncHttpClient;
    private HttpHandler httpHandler;

    @Override
    public void init(FunctionContext context) {
        logger.info("Creating Connection");
        this.asyncHttpClient = new AsyncHttpClient();
        this.httpHandler = new HttpHandler();
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.prepareGet(context.getParameterAsString(IP_PARAM_URL));
        this.httpHandler.setFunctionContext(context);

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
        return parameters;
    }
}
