package perf.agent.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 31/12/12
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobProcessorConfig {
    private int maxJobs;
    private int checkInterval;
    private String jobCLIFormat;

    public int getMaxJobs() {
        return maxJobs;
    }

    public void setMaxJobs(int maxJobs) {
        this.maxJobs = maxJobs;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }

    public String getJobCLIFormat() {
        return jobCLIFormat;
    }

    public void setJobCLIFormat(String jobCLIFormat) {
        this.jobCLIFormat = jobCLIFormat;
    }
}
