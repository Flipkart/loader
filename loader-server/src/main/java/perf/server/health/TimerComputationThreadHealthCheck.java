package perf.server.health;

import com.yammer.metrics.core.HealthCheck;
import perf.server.daemon.TimerComputationThread;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 18/6/13
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimerComputationThreadHealthCheck extends HealthCheck {
    public TimerComputationThreadHealthCheck(String name) {
        super(name);
    }

    @Override
    protected HealthCheck.Result check() throws Exception {
        return TimerComputationThread.instance().isAlive() ?
                HealthCheck.Result.healthy("TimerComputationThread is Alive") :
                HealthCheck.Result.unhealthy("TimerComputationThread is Dead");
    }
}
