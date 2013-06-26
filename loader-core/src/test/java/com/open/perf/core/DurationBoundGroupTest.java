package com.open.perf.core;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Load;
import com.open.perf.operation.MathAddFunction;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 26/6/13
 * Time: 10:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class DurationBoundGroupTest {
    @Test
    public void test() throws Exception {
        Load load = new Load().
                addGroup(new Group().
                        setName("G1").
                        setThreads(1).
                        addFunction(new GroupFunction().
                                setFunctionClass(MathAddFunction.class.getCanonicalName()).
                                setFunctionalityName("List Add").
                                addParam(MathAddFunction.IP_NUM_JSON_LIST, "[1,2,3,4,5,6,7,8,9,10]")));
        load.start(""+System.currentTimeMillis());
    }
}
