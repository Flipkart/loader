package com.open.perf.sample.run;

import com.open.perf.core.GroupController;
import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;

import java.util.UUID;

public class GroupControllerChangeThreadsTest {
    public static void main (String[]args) throws Exception {

        long startTime = System.currentTimeMillis();
        Group g1 = new Group("G1").
                setGroupStartDelay(0).
                setRepeats(10000).
                setThreads(10).
                addFunction(new GroupFunction("F1").
                        setFunctionClass("com.open.perf.sample.function.DelayFunction").
                        addParam("delay", "10").
                        dumpData()).
                addFunctionTimer("timer1");

        GroupController controller = new GroupController(UUID.randomUUID().toString(),g1);
        controller.start();

        int newThreads = 11;
        int multiplier = 1;
        while(controller.isAlive()) {
            System.out.println(controller.isAlive());
            Thread.sleep(100);
            controller.setThreads(newThreads+=(1 * multiplier));
            if(newThreads > 30)
                multiplier = -1;
            if(newThreads < 10)
                multiplier = 1;
        }
        System.out.println("Over :"+(System.currentTimeMillis() - startTime));

    }
}
