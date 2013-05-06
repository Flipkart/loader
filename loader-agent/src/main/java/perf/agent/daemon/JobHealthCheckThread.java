package perf.agent.daemon;

import com.open.perf.jackson.ObjectMapperUtil;
import com.open.perf.jmx.JMXConnection;
import com.open.perf.util.Clock;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.agent.client.LoaderServerClient;
import perf.agent.config.JobProcessorConfig;
import perf.agent.job.AgentJob;
import perf.agent.util.SystemInfo;

import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class JobHealthCheckThread extends Thread {
    private List<AgentJob> agentJobs;
    private Map<String, JMXConnection> jobJmxConnectionMap;
    private Map<String, JobHealthStatus> jobHealthStatusMap;
    private final LoaderServerClient serverClient;
    private static JobHealthCheckThread instance;
    private static Logger logger = LoggerFactory.getLogger(JobHealthCheckThread.class);
    private final JobProcessorConfig jobProcessorConfig;

    private JobHealthCheckThread(LoaderServerClient serverClient, JobProcessorConfig jobProcessorConfig) {
        this.agentJobs = new LinkedList<AgentJob>();
        this.jobJmxConnectionMap = new HashMap<String, JMXConnection>();
        this.jobHealthStatusMap = new HashMap<String, JobHealthStatus>();
        this.serverClient = serverClient;
        this.jobProcessorConfig = jobProcessorConfig;
        JobHealthStatus.CPU_USAGE_STRESS_LEVEL = jobProcessorConfig.getCpuUsageThreshold();
        JobHealthStatus.MEMORY_USAGE_STRESS_LEVEL = jobProcessorConfig.getMemoryUsageThreshold();
    }

    public static JobHealthCheckThread initialize(LoaderServerClient serverClient, JobProcessorConfig jobProcessorConfig) {
        if(instance == null) {
            instance = new JobHealthCheckThread(serverClient, jobProcessorConfig);
            instance.start();
        }
        return instance;
    }

    public void run() {
        while(true) {
            for(AgentJob agentJob : agentJobs) {
                JMXConnection jobJMXConnection = jobJmxConnectionMap.get(agentJob.getJobId());
                if(jobJMXConnection == null) {
                    try {
                        jobJMXConnection = new JMXConnection("localhost", agentJob.getJmxPort());
                        jobJmxConnectionMap.put(agentJob.getJobId(), jobJMXConnection);
                        JobHealthStatus jobHealthStatus = new JobHealthStatus();

                        jobHealthStatus.
                                setPreviousUpTime(jobJMXConnection.getRuntimeMXBean().getUptime()).
                                setPreviousProcessCpuTime(jobJMXConnection.getOperatingSystemMXBean().getProcessCpuTime());

                        jobHealthStatusMap.put(agentJob.getJobId(), jobHealthStatus);

                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
                else {

                    JobHealthStatus jobHealthStatus = jobHealthStatusMap.get(agentJob.getJobId());
                    boolean jobWasInStress = jobHealthStatus.inStress;
                    jobHealthStatus.setInStress(false);

                    try {
                        long currentUpTime = jobJMXConnection.getRuntimeMXBean().getUptime();
                        long currentProcessCpuTime = jobJMXConnection.getOperatingSystemMXBean().getProcessCpuTime();

                        // Calculation happens only from next iteration
                        // Calculate CPU Utilization
                        if (jobHealthStatus.getPreviousUpTime() > 0L && currentUpTime > jobHealthStatus.getPreviousUpTime()) {
                            // elapsedCpu is in ns and elapsedTime is in ms.
                            long elapsedCpu = currentProcessCpuTime - jobHealthStatus.getPreviousProcessCpuTime();
                            long elapsedTime = currentUpTime - jobHealthStatus.getPreviousUpTime();

                            jobHealthStatus.setCpuUsage(Math.min(100F,
                                    elapsedCpu / (elapsedTime * 10000F * JobHealthStatus.getNoOfCPUs())));
                        }

                        //Calculate Memory Utilization
                        MemoryUsage memoryUsage = jobJMXConnection.getMemoryMXBean().getHeapMemoryUsage();
                        jobHealthStatus.setMemoryUsage(memoryUsage.getUsed() * 100f / memoryUsage.getMax());

                        //Find DeadLocked Threads count
                        long[] deadlockedThreads = jobJMXConnection.getThreadMXBean().findDeadlockedThreads();
                        jobHealthStatus.setDeadLockedThreads(deadlockedThreads != null ? deadlockedThreads.length : 0);

                        jobHealthStatus.
                                setPreviousUpTime(currentUpTime).
                                setPreviousProcessCpuTime(currentProcessCpuTime).
                                setTime(System.currentTimeMillis());


                        // Publish job health status if required
                        if(jobHealthStatus.inStress) {
                            serverClient.notifyJobHealth(agentJob.getJobId(), ObjectMapperUtil.instance().writeValueAsString(jobHealthStatus));
                        }
                        else {
                            if(jobWasInStress) {
                                serverClient.notifyJobHealth(agentJob.getJobId(), ObjectMapperUtil.instance().writeValueAsString(jobHealthStatus));
                            }
                        }

                    } catch (IOException e) {
                        logger.error("", e);
                    } catch (InterruptedException e) {
                        logger.error("", e);
                    } catch (ExecutionException e) {
                        logger.error("", e);
                    }
                }
            }
            try {
                Clock.sleep(jobProcessorConfig.getHealthCheckInterval());
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }

    public static JobHealthCheckThread instance() {
        return instance;
    }

    public void addJob(AgentJob agentJob) {
        synchronized (agentJobs) {
            agentJobs.add(agentJob);
        }
    }

    public void removeJob(AgentJob agentJob) {
        synchronized (agentJobs) {
            agentJobs.remove(agentJob);
        }
    }

    public static class JobHealthStatus {
        private static int noOfCPUs;
        public static float CPU_USAGE_STRESS_LEVEL;
        public static float MEMORY_USAGE_STRESS_LEVEL;
        static {
            try {
                noOfCPUs = SystemInfo.getProcessors();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private long time;
        private long previousUpTime = -1L, previousProcessCpuTime = -1L;
        private float cpuUsage, memoryUsage;
        private int deadLockedThreads;
        private boolean inStress = false;

        public float getCpuUsage() {
            return cpuUsage;
        }

        public JobHealthStatus setCpuUsage(float cpuUsage) {
            this.cpuUsage = cpuUsage;
            inStress = inStress || cpuUsage >= CPU_USAGE_STRESS_LEVEL;
            return this;
        }

        public float getMemoryUsage() {
            return memoryUsage;
        }

        public JobHealthStatus setMemoryUsage(float memoryUsage) {
            this.memoryUsage = memoryUsage;
            inStress = inStress || memoryUsage >= MEMORY_USAGE_STRESS_LEVEL;
            return this;
        }

        public int getDeadLockedThreads() {
            return deadLockedThreads;
        }

        public JobHealthStatus setDeadLockedThreads(int deadLockedThreads) {
            this.deadLockedThreads = deadLockedThreads;
            inStress = inStress || deadLockedThreads > 0;
            return this;
        }

        public static int getNoOfCPUs() {
            return noOfCPUs;
        }

        public static void setNoOfCPUs(int noOfCPUs) {
            JobHealthStatus.noOfCPUs = noOfCPUs;
        }

        @JsonIgnore
        public long getPreviousUpTime() {
            return previousUpTime;
        }

        public JobHealthStatus setPreviousUpTime(long previousUpTime) {
            this.previousUpTime = previousUpTime;
            return this;
        }

        @JsonIgnore
        public long getPreviousProcessCpuTime() {
            return previousProcessCpuTime;
        }

        public JobHealthStatus setPreviousProcessCpuTime(long previousProcessCpuTime) {
            this.previousProcessCpuTime = previousProcessCpuTime;
            return this;
        }

        public boolean isInStress() {
            return inStress;
        }

        public JobHealthStatus setInStress(boolean inStress) {
            this.inStress = inStress;
            return this;
        }

        public long getTime() {
            return time;
        }

        public JobHealthStatus setTime(long time) {
            this.time = time;
            return this;
        }
    }
}
