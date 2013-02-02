package perf.agent.job;

import org.apache.log4j.Logger;
import perf.agent.util.SocketHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 30/12/12
 * Time: 7:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobRunner extends Thread{
    private JobInfo jobInfo;
    private Process jobProcess;
    private boolean running = false;

    private static Logger log = Logger.getLogger(JobRunner.class);
    public JobRunner(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
        start();
    }

    public void run() {
        log.info("Running Job :"+jobInfo.getJobId());
        this.running = true;
        try {
            this.jobInfo.setPort(SocketHelper.getFreePort(10000, 10010));
            String jobCmd = jobInfo.getJobCmd().
                    replace("$PORT", String.valueOf(jobInfo.getPort())).
                    replace("$JOB_ID", jobInfo.getJobId());

            log.info("Running Command \n"+jobCmd);
            jobProcess = Runtime.getRuntime().exec(jobCmd);
            new JobStdOutThread();
            new JobStdErrThread();
            jobProcess.waitFor();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        log.info("Job :"+jobInfo.getJobId() +" Ended");
        this.running = false;
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
                        log.info(line);
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
                        log.error(line);
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
