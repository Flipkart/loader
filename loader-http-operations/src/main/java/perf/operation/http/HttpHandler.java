package perf.operation.http;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import com.flipkart.perf.core.FunctionContext;


class HttpHandler extends AsyncCompletionHandler<Response> {
    private FunctionContext functionContext;

    public HttpHandler(FunctionContext functionContext) {
        this.functionContext = functionContext;
    }

    public HttpHandler() {}

    public void setFunctionContext(FunctionContext functionContext) {
        this.functionContext = functionContext;
    }

    @Override
    public Response onCompleted(Response response) throws Exception {
        this.functionContext.join();
        return response;
    }
}

