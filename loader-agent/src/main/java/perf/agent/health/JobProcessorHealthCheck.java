package perf.agent.health;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 26/10/12
 * Time: 9:05 AM
 * To change this template use File | Settings | File Templates.
 */
import com.yammer.metrics.core.HealthCheck;
import perf.agent.job.JobProcesser;

public class JobProcessorHealthCheck extends HealthCheck {

    public JobProcessorHealthCheck(String name) {
        super(name);
    }

    @Override
    protected Result check() throws Exception {
        return JobProcesser.getInstance().isAlive() ?
                Result.healthy("Job Processor Thread is Alive") :
                Result.unhealthy("Job Processor Thread is dead");
    }
}