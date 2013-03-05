package com.open.perf.load;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;
import org.testng.annotations.Test;

public class TestClass {
    @Test
    public void test() throws Exception {
        new Loader("L1").
                addGroup(new Group("G1").
                        setGroupStartDelay(0).
                        setRepeats(10000).
                        setThreads(10).
                        addFunction(new GroupFunction("F1").
                                setClassName("com.open.perf.sample.function.SampleFunction").
                                addParam("paramF1", "value1").
                                dumpData()).
                        addFunction(new GroupFunction("F2").
                                setClassName("com.open.perf.sample.function.SampleFunction").
                                addParam("paramF2", "value2").
                                dumpData()).
                        addFunctionTimer("logger.info").
                        addFunctionTimer("sleeper").
                        addFunctionCounter("testCounter").
                        setDumpDataAfterRepeats(5000)).
                start();
    }
}
