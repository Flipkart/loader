package perf.operation.http.util;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ProxyServer;
import com.open.perf.core.FunctionContext;
import perf.operation.http.constant.Constants;

import java.io.IOException;
import java.util.Map;

public class HttpRequestHelper {
    public static void addHeaders(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) throws IOException {
        Map<String, Object> headers = context.getParameterAsMap(Constants.IP_HEADERS);
        for(String header : headers.keySet()) {
            requestBuilder.addHeader(header, headers.get(header).toString());
        }
    }

    public static void addQueryParameters(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) throws IOException {
        Map<String, Object> queryParameters = context.getParameterAsMap(Constants.IP_QUERY_PARAMETERS);
        for(String query : queryParameters.keySet()) {
            requestBuilder.addQueryParameter(query, queryParameters.get(query).toString());
        }
    }

    public static void addParameters(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) throws IOException {
        Map<String, Object> parameters = context.getParameterAsMap(Constants.IP_PARAMETERS);
        for(String parameter : parameters.keySet()) {
            requestBuilder.addParameter(parameter, parameters.get(parameter).toString());
        }
    }

    public static void addProxy(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) throws IOException {
        Map<String, Object> proxyInfoMap = context.getParameterAsMap(Constants.IP_PROXY);
        requestBuilder.setProxyServer(new ProxyServer(proxyInfoMap.get("host").toString(),
                Integer.parseInt(proxyInfoMap.get("port").toString())));
    }

    public static void setFollowRedirects(FunctionContext context, AsyncHttpClient.BoundRequestBuilder requestBuilder) {
        requestBuilder.setFollowRedirects(context.getParameterAsBoolean(Constants.IP_FOLLOW_REDIRECTS));
    }
}
