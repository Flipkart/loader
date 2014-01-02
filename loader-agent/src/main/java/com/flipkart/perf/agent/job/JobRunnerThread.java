package com.flipkart.perf.agent.job;

import com.flipkart.perf.common.util.FileHelper;
import com.flipkart.perf.common.util.SocketHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.flipkart.perf.agent.config.JobFSConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Runs a Performance Job and Monitor it till it gets over
 */
public class JobRunnerThread extends Thread{
    private AgentJob agentJob;
    private Process jobProcess;
    private boolean running = false;

    private static Logger logger = LoggerFactory.getLogger(JobRunnerThread.class);
    private final JobFSConfig jobFSConfig;

    public JobRunnerThread(AgentJob agentJob, JobFSConfig jobFSConfig) {
        this.agentJob = agentJob;
        this.jobFSConfig = jobFSConfig;
        start();
    }

    public void run() {
        logger.info("Running Job :"+ agentJob.getJobId());
        this.running = true;
        try {
            this.agentJob.setJmxPort(SocketHelper.getFreePort(10000, 10010));
            this.agentJob.setHttpPort(SocketHelper.getFreePort(this.agentJob.getJmxPort()+1, 10010));
            agentJob.setJobCmd(
                    agentJob.getJobCmd().
                            replace("{jmxPort}", String.valueOf(agentJob.getJmxPort())).
                            replace("{httpPort}", String.valueOf(agentJob.getHttpPort())).
                            replace("{jobId}", agentJob.getJobId()));

            FileHelper.createFilePath(jobFSConfig.getJobLogFile(agentJob.getJobId()));
            logger.info("Running Command \n"+ agentJob.getJobCmd());
            jobProcess = Runtime.getRuntime().exec(new String[]{
                    "/bin/sh",
                    "-c",
                    agentJob.getJobCmd()});

            this.agentJob.started();
            new JobStdOutThread();
            new JobStdErrThread();
            jobProcess.waitFor();
            if(jobProcess.exitValue() == 0)
                this.agentJob.completed();
            else
                this.agentJob.errored();

        } catch (Exception e) {
            logger.error("Error While Running/Monitoring the job",e);
        } finally {
            logger.info("Job :"+ agentJob.getJobId() +" Ended");
            this.running = false;
        }
    }

    public boolean running() {
        return this.running;  //To change body of created methods use File | Settings | File Templates.
    }

    class JobStdOutThread extends Thread {
        public JobStdOutThread() {
            start();
        }

        public void run() {
            while(running) {
                BufferedReader br = new BufferedReader(new InputStreamReader(jobProcess.getInputStream()));
                String line;
                try {
                    while((line = br.readLine()) != null) {
                        logger.info(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    class JobStdErrThread extends Thread {
        public JobStdErrThread() {
            start();
        }

        public void run() {
            while(running) {
                BufferedReader br = new BufferedReader(new InputStreamReader(jobProcess.getErrorStream()));
                String line;
                try {
                    while((line = br.readLine()) != null) {
                        logger.error(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    public AgentJob getAgentJob() {
        return agentJob;
    }

    public void setAgentJob(AgentJob agentJob) {
        this.agentJob = agentJob;
    }

    public Process getJobProcess() {
        return jobProcess;
    }

    public void setJobProcess(Process jobProcess) {
        this.jobProcess = jobProcess;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("/home/nitinka/git/loader2.0/sleep.sh");
        Thread.sleep(10 * 1000);
        p.destroy();
        p.waitFor();
        System.out.println(p.exitValue());
    }
}
