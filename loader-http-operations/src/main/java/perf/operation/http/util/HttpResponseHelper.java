package perf.operation.http.util;

import com.ning.http.client.Response;
import com.open.perf.core.FunctionContext;
import perf.operation.http.constant.Constants;

import java.io.IOException;

import static com.open.perf.core.FunctionContext.FailureType.FUNCTIONAL_FAILURE;

public class HttpResponseHelper {
    public static boolean successfulRequest(FunctionContext context, Response response) {
        boolean success = (response.getStatusCode() == context.getParameterAsInteger(Constants.IP_EXPECTED_STATUS_CODE));
        if(!success) {
            context.failed(FUNCTIONAL_FAILURE,"Status code "+response.getStatusCode());
        }
        return success;
    }

    public static void passOnResponse(FunctionContext context, Response response) throws IOException {
        if(context.getParameterAsBoolean(Constants.IP_PASS_ON_BODY))
            context.addParameter(Constants.OP_HTTP_RESPONSE, response.getResponseBody());
    }

}
