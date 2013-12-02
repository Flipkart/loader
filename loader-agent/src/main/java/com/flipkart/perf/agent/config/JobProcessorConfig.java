package com.flipkart.perf.agent.config;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 31/12/12
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobProcessorConfig {
    private int maxJobs, pendingJobCheckInterval,healthCheckInterval;
    private float memoryUsageThreshold, cpuUsageThreshold;
    private String jobCLIFormat;

    public int getMaxJobs() {
        return maxJobs;
    }

    public void setMaxJobs(int maxJobs) {
        this.maxJobs = maxJobs;
    }

    public int getPendingJobCheckInterval() {
        return pendingJobCheckInterval;
    }

    public void setPendingJobCheckInterval(int pendingJobCheckInterval) {
        this.pendingJobCheckInterval = pendingJobCheckInterval;
    }

    public String getJobCLIFormat() {
        return jobCLIFormat;
    }

    public void setJobCLIFormat(String jobCLIFormat) {
        this.jobCLIFormat = jobCLIFormat;
    }

    public int getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public void setHealthCheckInterval(int healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
    }

    public float getMemoryUsageThreshold() {
        return memoryUsageThreshold;
    }

    public void setMemoryUsageThreshold(float memoryUsageThreshold) {
        this.memoryUsageThreshold = memoryUsageThreshold;
    }

    public float getCpuUsageThreshold() {
        return cpuUsageThreshold;
    }

    public void setCpuUsageThreshold(float cpuUsageThreshold) {
        this.cpuUsageThreshold = cpuUsageThreshold;
    }
}
