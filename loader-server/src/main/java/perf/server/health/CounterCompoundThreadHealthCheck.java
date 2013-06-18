package perf.server.health;

import com.yammer.metrics.core.HealthCheck;
import perf.server.daemon.CounterCompoundThread;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 18/6/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class CounterCompoundThreadHealthCheck extends HealthCheck{
    public CounterCompoundThreadHealthCheck(String name) {
        super(name);
    }

    @Override
    protected Result check() throws Exception {
        return CounterCompoundThread.instance().isAlive() ? Result.healthy("CounterCompoundThread is Alive") : Result.unhealthy("CounterCompoundThread is Dead");
    }
}
