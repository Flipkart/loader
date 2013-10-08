package perf.operation.common.print;

import com.flipkart.perf.core.FunctionContext;
import com.flipkart.perf.function.PerformanceFunction;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 5/4/13
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class PrintAllContextParams extends PerformanceFunction{
    @Override
    public void execute(FunctionContext context) throws Exception {
        System.out.println(context.getParameters());
    }
}
