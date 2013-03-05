package com.open.perf.sample.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;

public class TestClass {
    public static void main (String[]args) throws Exception {
        new Loader("L1").
                addGroup(new Group("G1").
                        setGroupStartDelay(0).
                        setRepeats(100000).
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
/*
                addGroup(new Group("G2").
                        dependsOn("G1").
                        setGroupStartDelay(0).
                        setRepeats(10).
                        setThreads(10).
                        addFunction(new GroupFunction("F3").
                                setClassName("com.open.perf.sample.function.SampleFunction").
                                addParam("paramF1", "value1").
                                dumpData()).
                        addFunction(new GroupFunction("F4").
                                setClassName("com.open.perf.sample.function.SampleFunction").
                                addParam("paramF2", "value2").
                                dumpData()).
                        addFunctionTimer("logger.info").
                        addFunctionTimer("sleeper").
                        addFunctionCounter("testCounter").
                        setDumpDataAfterRepeats(5000)).
*/
                start();
    }
}
