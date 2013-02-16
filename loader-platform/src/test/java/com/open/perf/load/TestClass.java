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
                        setLife(1000).
                        setRepeats(1).
                        setThreads(10).
                        addFunction(new GroupFunction("F1").
                                setClassName("com.open.perf.sample.SampleFunction").
                                addParam("paramF1", "value1").
                                dumpData()).
                        addFunction(new GroupFunction("F1").
                                setClassName("com.open.perf.sample.SampleFunction").
                                addParam("paramF2", "value1").
                                dumpData()).
                        addFunctionTimer("logger.info").
                        addFunctionTimer("sleeper").
                        addFunctionCounter("testCounter").
                        setDumpDataAfterRepeats(250)).
                start();
    }
}
