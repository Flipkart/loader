package com.open.perf.load;

import com.open.perf.domain.GroupBean;
import com.open.perf.domain.GroupFunctionBean;
import com.open.perf.domain.Loader;
import org.testng.annotations.Test;

public class TestClass {
    @Test
    public void test() throws Exception {
        new Loader("L1").
                addGroup(new GroupBean("G1").
                        setGroupStartDelay(0).
                        setLife(1000).
                        setRepeats(1).
                        setThreads(10).
                        addFunction(new GroupFunctionBean("F1").
                                setClassName("com.open.perf.operation.SampleFunction").
                                addParam("paramF1", "value1").
                                dumpData()).
                        addFunction(new GroupFunctionBean("F2").
                                setClassName("com.open.perf.operation.SampleFunction").
                                addParam("paramF2", "value1").
                                dumpData()).
                        addFunctionTimer("logger.info").
                        addFunctionTimer("sleeper").
                        addFunctionCounter("testCounter").
                        setDumpDataAfterRepeats(250)).
                start();
    }
}
