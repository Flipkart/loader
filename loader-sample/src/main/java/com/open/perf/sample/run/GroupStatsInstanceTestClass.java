package com.open.perf.sample.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;
import com.open.perf.sample.function.DelayFunction;
import com.open.perf.sample.function.DummyFunction;

import java.util.UUID;

public class GroupStatsInstanceTestClass {
    public static void main (String[]args) throws Exception {
        new Loader("Run").
                setJobId(UUID.randomUUID().toString()).
                addGroup(
                        new Group("G1").
                                setGroupStartDelay(0).
                                setDuration(60000).
                                setThreads(10).
                                addFunction(new GroupFunction("RandomDelay").
                                        setFunctionClass(DummyFunction.class.getCanonicalName()).
                                        addParam("delay", 5).dumpData()).
                                addFunctionCounter("counter1").
                                addFunctionTimer("timer1")).
/*
                addGroup(
                        new Group("G2").
                                setGroupStartDelay(0).
                                setDuration(5000).
                                setThreads(10).
                                addFunction(new GroupFunction("RandomDelay").
                                        setFunctionClass("com.open.perf.sample.function.DelayFunction").
                                        addParam("delay", 15).
                                        dumpData()).
                                addFunctionCounter("counter1").
                                addFunctionTimer("timer1")).
*/
                start();

    }
}
