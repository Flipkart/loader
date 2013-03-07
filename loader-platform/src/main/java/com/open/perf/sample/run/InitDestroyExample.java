package com.open.perf.sample.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;

import java.util.UUID;

public class InitDestroyExample {
    public static void main (String[]args) throws Exception {
        new Loader("Run").
                setJobId(UUID.randomUUID().toString()).
                addGroup(
                        new Group("G1").
                                setGroupStartDelay(0).
                                setRepeats(1).
                                setThreads(1).
                                addFunction(new GroupFunction("RandomDelay").
                                        setClassName("com.open.perf.sample.function.DelayFunction").
                                        addParam("delay", 5)).
                                addFunctionCounter("counter1").
                                addFunctionTimer("timer1")).
                start();
    }
}
