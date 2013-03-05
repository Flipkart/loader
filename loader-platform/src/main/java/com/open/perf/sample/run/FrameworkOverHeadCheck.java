package com.open.perf.sample.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;

public class FrameworkOverHeadCheck {
    public static void main (String[]args) throws Exception {
        new Loader("Run").addGroup(
                new Group("G1").
                        setGroupStartDelay(0).
                        setRepeats(10000).
                        setThreads(10).
                        setDumpDataAfterRepeats(1000).
                        addFunction(new GroupFunction("RandomDelay").
                                setClassName("com.open.perf.sample.function.DelayFunction").
                                addParam("delay", 1).
                                dumpData())).
                start();
    }


}
