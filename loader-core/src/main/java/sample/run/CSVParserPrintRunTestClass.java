package sample.run;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Loader;
import com.open.perf.function.data.CSVDataParser;
import com.open.perf.function.print.PlainPrintParam;

import java.io.File;
import java.util.UUID;

public class CSVParserPrintRunTestClass {
    public static void main (String[]args) throws Exception {
        System.out.println(new File(".").getAbsolutePath());
        new Loader("HttpGetRun").
                setJobId(UUID.randomUUID().toString()).
                addGroup(
                        new Group("CsvPrint").
                                setGroupStartDelay(0).
                                setRepeats(10).
                                setThreads(1).
                                addFunction(new GroupFunction("CSV").
                                        setFunctionClass(CSVDataParser.class.getCanonicalName()).
                                        addParam(CSVDataParser.IP_CSV_FILE, args[0])).
                                addFunction(new GroupFunction("PlainPrint").
                                        setFunctionClass(PlainPrintParam.class.getCanonicalName()))).
                start();

    }
}
