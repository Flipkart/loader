package perf.operation.http.util;

import com.ning.http.client.*;
import com.open.perf.core.FunctionContext;
import com.open.perf.function.FunctionParameter;
import org.codehaus.jackson.map.ObjectMapper;
import perf.operation.http.constant.Constants;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HttpRequestHelper {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static AsyncHttpClient buildClient() {
        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setAllowPoolingConnection(true).
                setMaximumConnectionsTotal(1000).
                setCompressionEnabled(true).
                setRequestTimeoutInMs(120000);
        return new AsyncHttpClient(builder.build());
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
        Map<String, Object> parameters = context.getParameterAsMap(Constants.IP_PARAMETERS);
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
        requestBuilder.setFollowRedirects(context.getParameterAsBoolean(Constants.IP_FOLLOW_REDIRECTS));
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
    
}
