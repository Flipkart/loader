package com.open.perf.sample.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;

public class DelayTestClass {
    public static void main (String[]args) throws Exception {
        new Loader("L1").
                addGroup(new Group("G1").
                        setGroupStartDelay(0).
                        setRepeats(10000).
                        setDuration(500).
                        setThreads(1).
                        addFunction(new GroupFunction("RandomDelay").
                                setClassName("com.open.perf.sample.function.DelayFunction").
                                addParam("delay", 10).
                                dumpData()).
                        setDumpDataAfterRepeats(5000).
                        addFunctionCounter("counter1").
                        addFunctionTimer("timer1")).
                start();
    }
}
