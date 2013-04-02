package perf.operation.http.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;
import perf.operation.http.HttpGet;

import java.util.UUID;

public class HttpGetRunTestClass {
    public static void main (String[]args) throws Exception {
        new Loader("HttpGetRun").
                setJobId(UUID.randomUUID().toString()).
                addGroup(
                        new Group("HttpGet").
                                setGroupStartDelay(0).
                                setDuration(Integer.parseInt(args[0])).
                                setRepeats(Integer.parseInt(args[1])).
                                setThreads(Integer.parseInt(args[2])).
                                setThroughput(Integer.parseInt(args[3])).
                                addFunction(new GroupFunction("HttpGet").
                                        setFunctionClass(HttpGet.class.getCanonicalName()).
                                        addParam(HttpGet.IP_PARAM_URL,"http://localhost:9999/loader-server/agents").
                                        addParam(HttpGet.IP_PASS_ON_BODY, false).
                                        addParam(HttpGet.IP_EXPECTED_STATUS_CODE, 200).
                                        dumpData())).
                start();

    }
}
