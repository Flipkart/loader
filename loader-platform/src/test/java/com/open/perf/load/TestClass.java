package com.open.perf.load;

import com.open.perf.domain.GroupBean;
import com.open.perf.domain.GroupFunctionBean;
import com.open.perf.domain.Loader;
import org.testng.annotations.Test;

import java.lang.System;
import java.util.HashMap;
import java.util.Map;

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
                                addParam("param1", "value1").
                                dumpData()).
                        addFunctionTimer("logger.info").
                        addFunctionTimer("sleeper").
                        addFunctionCounter("testCounter").
                        setDumpDataAfterRepeats(250)).
                start();
    }
}
