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
    private JobInfo jobInfo;
    private Process jobProcess;
    private boolean running = false;

    private static Logger logger = LoggerFactory.getLogger(JobRunnerThread.class);
    private final JobFSConfig jobFSConfig;

    public JobRunnerThread(JobInfo jobInfo, JobFSConfig jobFSConfig) {
        this.jobInfo = jobInfo;
        this.jobFSConfig = jobFSConfig;
        start();
    }

    public void run() {
        logger.info("Running Job :"+jobInfo.getJobId());
        this.running = true;
        try {
            this.jobInfo.setPort(SocketHelper.getFreePort(10000, 10010));
            String jobCmd = jobInfo.getJobCmd().
                    replace("{port}", String.valueOf(jobInfo.getPort())).
                    replace("{portId}", jobInfo.getJobId());

            FileHelper.createFilePath(jobFSConfig.getJobLogFile(jobInfo.getJobId()));
            logger.info("Running Command \n"+jobCmd);
            jobProcess = Runtime.getRuntime().exec(new String[]{
                    "/bin/sh",
                    "-c",
                    jobCmd});
            new JobStdOutThread();
            new JobStdErrThread();
            jobProcess.waitFor();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            logger.info("Job :"+jobInfo.getJobId() +" Ended");
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

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
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
}
