package perf.operation.http.util;

import com.ning.http.client.*;
import com.open.perf.core.FunctionContext;
import com.open.perf.function.FunctionParameter;
import com.open.perf.jackson.ObjectMapperUtil;
import org.codehaus.jackson.map.ObjectMapper;
import perf.operation.http.constant.Constants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpRequestHelper {
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();
    private static AsyncHttpClient asyncHttpClient;
    private static AtomicInteger counter = new AtomicInteger(0);

    public static AsyncHttpClient buildClient() {
        if(asyncHttpClient == null) {
            AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
            builder.setAllowPoolingConnection(true).
                    setMaximumConnectionsTotal(1000).
                    setCompressionEnabled(true).
                    setRequestTimeoutInMs(120000);
            asyncHttpClient = new AsyncHttpClient(builder.build());
        }

        counter.incrementAndGet();
        return asyncHttpClient;
    }

    public static void closeConnection() {
        if(counter.decrementAndGet() == 0)
            asyncHttpClient.close();
    }

    public static Response executeRequest(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) throws IOException, InterruptedException, ExecutionException {
        context.startMe();
        Future<Response> responseF = requestBuilder.execute();
        Response response = responseF.get();
        context.endMe();
        return response;
    }

    public static void enhanceRequest(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) throws IOException {
        addHeaders(context, requestBuilder);
        addQueryParameters(context, requestBuilder);
        addParameters(context, requestBuilder);
        addProxy(context, requestBuilder);
        setFollowRedirects(context, requestBuilder);
        addCookies(context, requestBuilder);
    }

    public static void addHeaders(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) throws IOException {
        Map<String, Object> headers = context.getParameterAsMap(Constants.IP_HEADERS);
        if(headers != null) {
            for(String header : headers.keySet()) {
                requestBuilder.addHeader(header, headers.get(header).toString());
            }
        }
    }

    public static void addQueryParameters(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) throws IOException {
        Map<String, Object> queryParameters = context.getParameterAsMap(Constants.IP_QUERY_PARAMETERS);
        if(queryParameters != null) {
            for(String query : queryParameters.keySet()) {
                requestBuilder.addQueryParameter(query, queryParameters.get(query).toString());
            }
        }
    }

    public static void addParameters(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) throws IOException {
        Map<String, String> parameters = context.getParameterAsMap(Constants.IP_PARAMETERS);
        if(parameters != null) {
            for(String parameter : parameters.keySet()) {
                requestBuilder.addParameter(parameter, parameters.get(parameter).toString());
            }
        }
    }

    public static void addProxy(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) throws IOException {
        Map<String, Object> proxyInfoMap = context.getParameterAsMap(Constants.IP_PROXY);
        if(proxyInfoMap != null && proxyInfoMap.size() > 0)
            requestBuilder.setProxyServer(new ProxyServer(proxyInfoMap.get("host").toString(),
                Integer.parseInt(proxyInfoMap.get("port").toString())));
    }

    public static void setFollowRedirects(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) {
        requestBuilder.setFollowRedirects(context.getParameterAsBoolean(Constants.IP_FOLLOW_REDIRECTS).booleanValue());
    }

    public static void addCookies(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) throws IOException {
        List<Object> cookies = context.getParameterAsList(Constants.IP_COOKIES);
        if(cookies != null) {
            for(Object cookie : cookies) {
                Map<String, Object> cookieInfoMap = objectMapper.readValue(cookie.toString(), Map.class);
                requestBuilder.addCookie(new Cookie("",
                        cookieInfoMap.get("name").toString(),
                        cookieInfoMap.get("value").toString(),
                        "",
                        100000,
                        true));
            }
        }
    }

    /**
     * Should be called only for http post and put calls
     * @param context
     * @param requestBuilder
     */
    public static void addRequestBody(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder)
            throws FileNotFoundException {
        Object bodyStringObj = context.getParameter(Constants.IP_BODY_STRING);
        if(bodyStringObj != null) {
            requestBuilder.setBody(bodyStringObj.toString());
            return;
        }
        else {
            Object bodyFileObj = context.getParameter(Constants.IP_BODY_FILE);
            if(bodyFileObj != null) {
                requestBuilder.setBody(new FileInputStream(bodyFileObj.toString()));
                return;
            }
        }

        throw new RuntimeException("For Http Put/Post Either '" +
                Constants.IP_BODY_STRING +
                "' or '" +
                Constants.IP_BODY_STRING +
                "' parameter is necessary");
    }

    /**
     * Should be called only for http post and put calls
     * @param context
     * @param requestBuilder
     */
    public static void setCharacterEncoding(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) {
        requestBuilder.setBodyEncoding(context.getParameterAsString(Constants.IP_BODY_ENCODING, "UTF-8"));
    }

    public static void addInputCookies(LinkedHashMap<String, FunctionParameter> parameters) {
        parameters.put(Constants.IP_COOKIES,
                        new FunctionParameter().
                                setName(Constants.IP_COOKIES).
                                setMandatory(false).
                                setDefaultValue("[]").
                                setDescription("Proxy for the request as json. Eg : [{'name' : 'value'}]"));
    }

    public static void addInputProxy(LinkedHashMap<String, FunctionParameter> parameters) {
        parameters.put(Constants.IP_PROXY,
                        new FunctionParameter().
                                setName(Constants.IP_PROXY).
                                setMandatory(false).
                                setDefaultValue("{}").
                                setDescription("Proxy for the request as json. Eg : {'host' : '127.0.0.1', 'port' : '1234'}"));
    }

    public static void addInputQueryParameters(LinkedHashMap<String, FunctionParameter> parameters) {
        parameters.put(Constants.IP_QUERY_PARAMETERS,
                        new FunctionParameter().
                                setName(Constants.IP_QUERY_PARAMETERS).
                                setMandatory(false).
                                setDefaultValue("{}").
                                setDescription("Http Request Query Parameters as json"));
    }

    public static void addInputParameters(LinkedHashMap<String, FunctionParameter> parameters) {
        parameters.put(Constants.IP_PARAMETERS,
                        new FunctionParameter().
                                setName(Constants.IP_PARAMETERS).
                                setMandatory(false).
                                setDefaultValue("{}").
                                setDescription("Http Request Parameters as json"));
    }

    public static void addInputHeaders(LinkedHashMap<String, FunctionParameter> parameters) {
        parameters.put(Constants.IP_HEADERS,
                        new FunctionParameter().
                                setName(Constants.IP_HEADERS).
                                setMandatory(false).
                                setDefaultValue("{}").
                                setDescription("Http Request Headers as json. Eg: {'content-type' : 'application/json', 'accept' : 'application/json'}"));
    }

    public static void addInputFollowRedirects(LinkedHashMap<String, FunctionParameter> parameters) {
        parameters.put(Constants.IP_FOLLOW_REDIRECTS,
                        new FunctionParameter().
                                setName(Constants.IP_FOLLOW_REDIRECTS).
                                setMandatory(false).
                                setDefaultValue(false).
                                setDescription("If you want to allow request redirects"));
    }

    public static void addInputExpectedStatusCode(LinkedHashMap<String, FunctionParameter> parameters) {
        parameters.put(Constants.IP_EXPECTED_STATUS_CODE,
                        new FunctionParameter().
                                setName(Constants.IP_EXPECTED_STATUS_CODE).
                                setMandatory(true).
                                setDefaultValue(200).
                                setDescription("Status code to validate"));
    }

    public static void addInputUrlParam(LinkedHashMap<String, FunctionParameter> parameters) {
        parameters.put(Constants.IP_PARAM_URL,
                new FunctionParameter().
                        setName(Constants.IP_PARAM_URL).
                        setMandatory(true).
                        setDefaultValue("").
                        setDescription("Url to do http Get"));
    }

    public static void addInputBodyString(LinkedHashMap<String,FunctionParameter> parameters) {
        parameters.put(Constants.IP_BODY_STRING,
                new FunctionParameter().
                        setName(Constants.IP_BODY_STRING).
                        setMandatory(false).
                        setDefaultValue("").
                        setDescription("Body for Put/Post. Use either of '" +
                                        Constants.IP_BODY_STRING +
                                        "' or '" +
                                        Constants.IP_BODY_STRING +
                                        "'"));
    }

    public static void addInputBodyFile(LinkedHashMap<String,FunctionParameter> parameters) {
        parameters.put(Constants.IP_BODY_FILE,
                new FunctionParameter().
                        setName(Constants.IP_BODY_FILE).
                        setMandatory(false).
                        setDefaultValue("").
                        setDescription("Body for Put/Post. Use either of '" +
                                        Constants.IP_BODY_STRING +
                                        "' or '" +
                                        Constants.IP_BODY_STRING +
                                        "'"));
    }

    public static void addInputCharacterEncoding(LinkedHashMap<String,FunctionParameter> parameters) {
        parameters.put(Constants.IP_BODY_ENCODING,
                new FunctionParameter().
                        setName(Constants.IP_BODY_ENCODING).
                        setMandatory(true).
                        setDefaultValue("UTF-8").
                        setDescription("Character Encoding for Body"));
    }

    public static void addInputPassOnBody(LinkedHashMap<String, FunctionParameter> parameters) {
        parameters.put(Constants.IP_PASS_ON_BODY,
                new FunctionParameter().
                        setName(Constants.IP_PASS_ON_BODY).
                        setMandatory(true).
                        setDefaultValue(false).
                        setDescription("If you want to pass the response body to next function"));
    }
}
