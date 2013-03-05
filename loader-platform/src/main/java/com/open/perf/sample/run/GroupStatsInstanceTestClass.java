package com.open.perf.sample.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;
import com.open.perf.load.GroupControllerNew;

public class GroupStatsInstanceTestClass {
    public static void main (String[]args) throws Exception {
        new Loader("Run").addGroup(
                new Group("G1").
                        setGroupStartDelay(0).
                        setRepeats(5000).
                        setThreads(1).
                        addFunction(new GroupFunction("RandomDelay").
                                setClassName("com.open.perf.sample.function.DelayFunction").
                                addParam("delay", 4).
                                dumpData()).
                        addFunctionCounter("counter1").
                        addFunctionTimer("timer1")).
                start();
    }


}
