package com.flipkart.server.monitor.collector;

import com.flipkart.perf.common.util.ProcessHelper;
import com.flipkart.server.monitor.domain.ResourceCollectionInstance;
import com.flipkart.server.monitor.exception.ProcessExecutionFailedException;

import java.io.IOException;
import java.util.*;

/**
 * Native Collector which collects stats provided by df command
 */
public class DFCollector extends NativeCmdBaseCollector {
    private static final String KEY_FORMAT = "%s.%s.1kblocks.%s";

    public DFCollector(String name, Map<String,Object> params,int interval) {
        super(name, params, interval);
    }

    @Override
    protected String getCmd() {
        /**
         * Handling only linux for the time being
         */
        switch(CLIENT_OS) {
            case LINUX:
                return "df -PlTk";
        }
        return null;
    }

    @Override
    public ResourceCollectionInstance collect() throws IOException, InterruptedException, ProcessExecutionFailedException {
        Process process = executeCmd();
        String cmdOutput = ProcessHelper.getOutput(process);

        ResourceCollectionInstance collectionInstance = new ResourceCollectionInstance().
                setResourceName(this.getName()).
                setTime(System.currentTimeMillis());

        String[] lines = cmdOutput.trim().split("\n");

        for(int lineNo=1; lineNo <lines.length; lineNo++) {
            Double total = 0d;
            Double used = 0d;
            Double free = 0d;
            String fsType = "";
            String mount = "";

            StringTokenizer tokenizer = new StringTokenizer(lines[lineNo]);
            for(int tokenI = 0 ;tokenizer.hasMoreTokens(); tokenI++) {
                String token = tokenizer.nextToken();
                switch(tokenI) {
                    case 1:
                        fsType = token;
                        break;
                    case 2:
                        total = Double.parseDouble(token);
                        break;
                    case 3:
                        used = Double.parseDouble(token);
                        break;
                    case 4:
                        free = Double.parseDouble(token);
                        break;
                    case 6:
                        mount = token;
                }
            }

            collectionInstance.
                    addMetric(String.format(KEY_FORMAT, fsType, mount, "total"), total).
                    addMetric(String.format(KEY_FORMAT, fsType, mount, "used"), used).
                    addMetric(String.format(KEY_FORMAT, fsType, mount, "free"), free);
        }
        return collectionInstance;
    }
}