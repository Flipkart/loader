package perf.operation.http.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;
import com.open.perf.function.data.CSVDataParser;
import perf.operation.http.HttpGet;

import java.util.UUID;

public class HttpGetCSVDrivenRunTestClass {
    public static void main (String[]args) throws Exception {
        new Loader("HttpGetRun").
                setJobId(UUID.randomUUID().toString()).
                addGroup(
                        new Group("HttpGet").
                                setGroupStartDelay(0).
                                setRepeats(10).
                                setThreads(1).
                                setThroughput(1).
                                addFunction(new GroupFunction("CSV").
                                        setFunctionClass(CSVDataParser.class.getCanonicalName()).
                                        addParam(CSVDataParser.IP_CSV_FILE,"./sample/sampleData.csv")).
                                addFunction(new GroupFunction("HttpGet").
                                        setFunctionClass(HttpGet.class.getCanonicalName()).
                                        addParam(HttpGet.IP_PARAM_URL,"http://localhost:9999/loader-server/agents").
                                        addParam(HttpGet.IP_PASS_ON_BODY, false).
                                        addParam(HttpGet.IP_EXPECTED_STATUS_CODE, 200).
                                        dumpData())).
                start();

    }
}
