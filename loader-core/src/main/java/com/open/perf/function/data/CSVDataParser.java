package com.open.perf.function.data;

import au.com.bytecode.opencsv.CSVReader;
import com.open.perf.core.FunctionContext;
import com.open.perf.function.FunctionParameter;
import com.open.perf.function.PerformanceFunction;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Can be used for data driven Performance Runs.
 * Pass in a csv file and
 */
public class CSVDataParser extends PerformanceFunction{
    private String[] headers;
    private CSVReader csvReader;
    public static final String IP_CSV_FILE = "csvFile";

    @Override
    public void init(FunctionContext context) throws IOException {
        initializeCSVReader(context);
    }

    @Override
    public void execute(FunctionContext context) throws Exception {
        String[] csvParameters = this.csvReader.readNext();
        if(csvParameters == null) {
            csvReader.close();
            initializeCSVReader(context);
            csvParameters = this.csvReader.readNext();
        }
        if(csvParameters != null) {
            for(int headerI=0; headerI<headers.length; headerI++) {
                context.addParameter(headers[headerI], csvParameters[headerI]);
            }
        }
    }

    @Override
    public void end(FunctionContext context) throws IOException {
        csvReader.close();
    }

    @Override
    public List<String> description() {
        return Arrays.asList(new String[]{"This Function is useful to do data driven performance testing.",
                "Every line in the csvfile would be passed to subsequent functions in the flow as key values pairs.",
                "Once csv file is completed it would be reused if run is not over yet."});
    }

    @Override
    public LinkedHashMap<String, FunctionParameter> inputParameters(){
        LinkedHashMap<String, FunctionParameter> inputParameters = new LinkedHashMap<String, FunctionParameter>();
        inputParameters.put(IP_CSV_FILE, new FunctionParameter().
                setName(IP_CSV_FILE).
                setMandatory(true).
                setDefaultValue("").
                setDescription("CSV Input file"));
        return inputParameters;
    }


    private void initializeCSVReader(FunctionContext context) throws IOException {
        this.csvReader = new CSVReader(new FileReader(context.getParameterAsString(IP_CSV_FILE)));
        this.headers = csvReader.readNext();
        if(headers == null) {
            throw new RuntimeException("no data found in input csv file");
        }
    }
}
