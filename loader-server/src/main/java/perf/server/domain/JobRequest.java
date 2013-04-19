package perf.server.domain;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 19/4/13
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class JobRequest {
    private String runName;

    public String getRunName() {
        return runName;
    }

    public JobRequest setRunName(String runName) {
        this.runName = runName;
        return this;
    }
}
