package server.monitor.collector;

import com.open.perf.util.ProcessHelper;
import server.monitor.domain.ResourceCollectionInstance;
import server.monitor.exception.ProcessExecutionFailedException;

import java.io.IOException;
import java.util.Map;

/**
 * Generic Resource Collector which is capable of executing Native Commands as Runtime.exec()
 */
public abstract class NativeCmdBaseCollector extends BaseCollector{
    private static final int MAX_CMD_TIME = 10000;
    public NativeCmdBaseCollector(String name, Map<String, Object> params, int interval) {
        super(name, params, interval);
    }

    public Process executeCmd() throws IOException, InterruptedException, ProcessExecutionFailedException {
        Process process = Runtime.getRuntime().exec(getCmd());

        if(!ProcessHelper.wait(process, MAX_CMD_TIME)) {
            process.destroy();
            throw new ProcessExecutionFailedException("Process Didn't complete in "+MAX_CMD_TIME+"ms");
        }

        if(process.exitValue() != 0)
            throw new ProcessExecutionFailedException(ProcessHelper.getError(process));

            return process;
    }

    @Override
    public boolean supported() {
        // Currently Going live only with LINUX
        if(CLIENT_OS != OS.LINUX)
            return false;

        boolean supported = true;
        try {
            executeCmd();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            supported = false;
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            supported = false;
        } catch (ProcessExecutionFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return supported;
    }

    /**
     * implement it the set OS related Command
     */
    protected abstract String getCmd();
    abstract public ResourceCollectionInstance collect() throws IOException, InterruptedException, ProcessExecutionFailedException;
}
