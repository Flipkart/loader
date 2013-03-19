package sample.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;
import sample.function.DummyFunction;

import java.util.UUID;

public class GroupStatsInstanceTestClass {
    public static void main (String[]args) throws Exception {
        System.out.println(new Loader("Run").
                setJobId(UUID.randomUUID().toString()).
                addGroup(
                        new Group("G1").
                                setGroupStartDelay(0).
                                setRepeats(1).
                                setThreads(1).
                                setThroughput(1).
                                addFunction(new GroupFunction("RandomDelay").
                                        setFunctionClass(DummyFunction.class.getCanonicalName()).
                                        dumpData())).
                start().
                getJobId());

    }
}
