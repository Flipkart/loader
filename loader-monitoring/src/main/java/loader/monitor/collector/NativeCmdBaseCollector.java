package loader.monitor.collector;

import loader.monitor.domain.ResourceCollectionInstance;
import loader.monitor.exception.ProcessExcutionFailedException;
import loader.monitor.util.ProcessHelper;

import java.io.IOException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 3/1/13
 * Time: 3:02 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class NativeCmdBaseCollector extends BaseCollector{
    private static final int MAX_CMD_TIME = 10000;
    public NativeCmdBaseCollector(String name, Map<String, Object> params, int interval) {
        super(name, params, interval);
    }

    public Process executeCmd() throws IOException, InterruptedException, ProcessExcutionFailedException {
        Process process = Runtime.getRuntime().exec(getCmd());

        if(!ProcessHelper.wait(process,MAX_CMD_TIME)) {
            process.destroy();
            throw new ProcessExcutionFailedException("Process Didn't complete in "+MAX_CMD_TIME+"ms");
        }

        if(process.exitValue() != 0)
            throw new ProcessExcutionFailedException(ProcessHelper.getError(process));

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
        } catch (ProcessExcutionFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return supported;
    }

    /**
     * implement it the set OS related Command
     */
    protected abstract String getCmd();
    abstract public ResourceCollectionInstance collect() throws IOException, InterruptedException, ProcessExcutionFailedException;
}
