package sample.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;
import org.codehaus.jackson.map.ObjectMapper;
import sample.function.DelayFunction;

import java.util.UUID;

public class InitDestroyExample {
    public static void main (String[]args) throws Exception {
        Loader loader = new Loader("SampleRun").
                setJobId(UUID.randomUUID().toString()).
                addGroup(
                        new Group("SampleGroup").
                                setGroupStartDelay(0).
                                setRepeats(10000000).
                                setThreads(5).
                                addFunction(new GroupFunction("SampleFunction").
                                        dumpData().
                                        setFunctionClass(DelayFunction.class.getCanonicalName()).addParam("delay",10)).
                                addFunctionCounter("counter1").
                                addFunctionTimer("timer1"));

        ObjectMapper mapper = new ObjectMapper();
        String loaderAsString = mapper.writeValueAsString(loader);
        System.out.println(loaderAsString);
        Loader fromString = mapper.readValue(loaderAsString, Loader.class);
        fromString.start();
    }
}
