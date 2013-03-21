package sample.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.GroupTimer;
import com.open.perf.domain.Loader;
import org.codehaus.jackson.map.ObjectMapper;
import sample.function.DummyFunction;

import java.util.UUID;

public class GroupStatsInstanceTestClass {
    public static void main (String[]args) throws Exception {
        Loader l = new Loader("Run").
                setJobId(UUID.randomUUID().toString()).
                addGroup(
                        new Group("G1").
                                setGroupStartDelay(0).
                                setRepeats(100).
                                setThreads(1).
                                setThroughput(1).
                                addFunction(new GroupFunction("RandomDelay").
                                        setFunctionClass(DummyFunction.class.getCanonicalName()).
                                        dumpData()).
                                addTimer(new GroupTimer().
                                        setDuration(2000).
                                        setName("T1").
                                        setThreads(1).
                                        setThroughput(1.5f)).
                                addTimer(new GroupTimer().
                                        setDuration(2000).
                                        setName("T2").
                                        setThreads(2).
                                        setThroughput(2.8f)).
                                addTimer(new GroupTimer().
                                        setDuration(2000).
                                        setName("T3").
                                        setThreads(3).
                                        setThroughput(3.9f)).
                                addTimer(new GroupTimer().
                                        setDuration(2000).
                                        setName("T4").
                                        setThreads(4).
                                        setThroughput(4.2f)));

        System.out.println(new ObjectMapper().writeValueAsString(l));

    }
}
