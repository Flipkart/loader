package perf.agent.job;

import com.open.perf.util.FileHelper;
import com.open.perf.util.SocketHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.agent.config.JobFSConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Runs a Performance Job and Monitor it till it gets over
 */
public class JobRunnerThread extends Thread{
    private Job job;
    private Process jobProcess;
    private boolean running = false;

    private static Logger logger = LoggerFactory.getLogger(JobRunnerThread.class);
    private final JobFSConfig jobFSConfig;

    public JobRunnerThread(Job job, JobFSConfig jobFSConfig) {
        this.job = job;
        this.jobFSConfig = jobFSConfig;
        start();
    }

    public void run() {
        logger.info("Running Job :"+ job.getJobId());
        this.running = true;
        try {
            this.job.setJmxPort(SocketHelper.getFreePort(10000, 10010));
            job.setJobCmd(
                    job.getJobCmd().
                    replace("{jmxPort}", String.valueOf(job.getJmxPort())).
                    replace("{jobId}", job.getJobId()));

            FileHelper.createFilePath(jobFSConfig.getJobLogFile(job.getJobId()));
            logger.info("Running Command \n"+job.getJobCmd());
            jobProcess = Runtime.getRuntime().exec(new String[]{
                    "/bin/sh",
                    "-c",
                    job.getJobCmd()});

            this.job.started();
            new JobStdOutThread();
            new JobStdErrThread();
            jobProcess.waitFor();
            if(jobProcess.exitValue() == 0)
                this.job.completed();
            else
                this.job.errored();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            logger.info("Job :"+ job.getJobId() +" Ended");
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
                    br.close();
                    Thread.sleep(1000);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
                    br.close();
                    Thread.sleep(1000);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
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
