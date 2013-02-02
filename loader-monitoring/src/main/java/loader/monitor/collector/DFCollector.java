package loader.monitor.collector;

import loader.monitor.domain.Metric;
import loader.monitor.exception.ProcessExcutionFailedException;
import loader.monitor.util.ProcessHelper;

import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 3/1/13
 * Time: 2:58 PM
 * To change this template use File | Settings | File Templates.
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
    public ResourceCollectionInstance collect() throws IOException, InterruptedException, ProcessExcutionFailedException {
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
                    addMetric(new Metric().
                            setName(String.format(KEY_FORMAT, fsType, mount, "total")).
                            setValue(total)).
                    addMetric(new Metric().
                            setName(String.format(KEY_FORMAT, fsType, mount, "used")).
                            setValue(used)).
                    addMetric(new Metric().
                            setName(String.format(KEY_FORMAT, fsType, mount, "free")).
                            setValue(free));

        }
        return collectionInstance;
    }
}