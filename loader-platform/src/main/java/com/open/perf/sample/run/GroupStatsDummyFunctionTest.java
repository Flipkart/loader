package com.open.perf.sample.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;

public class GroupStatsDummyFunctionTest {
    public static void main (String[]args) throws Exception {
        new Loader("L1").
                addGroup(new Group("G1").
                        setGroupStartDelay(0).
                        setRepeats(1500000).
                        setThreads(10).
                        addFunction(new GroupFunction("RandomDelay").
                                setClassName("com.open.perf.sample.function.DummyFunction").
                                dumpData()).
                        setDumpDataAfterRepeats(5000)).
                start();
    }
}
