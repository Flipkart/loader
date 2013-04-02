package perf.agent.daemon;

import com.open.perf.util.Clock;
import com.open.perf.util.FileHelper;
import org.apache.log4j.Logger;
import perf.agent.client.LoaderServerClient;
import perf.agent.config.JobStatSyncConfig;
import perf.agent.config.ServerInfo;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Agent Thread which is responsible for pushing all job stats back to server
 */
public class JobStatsSyncThread extends Thread{
    private List<String> jobIds;
    private JobStatSyncConfig syncConfig;
    private static JobStatsSyncThread jobStatsSyncThread;
    private ServerInfo serverInfo;
    private static Logger log = Logger.getLogger(JobStatsSyncThread.class);
    private LoaderServerClient serverClient;

    public static JobStatsSyncThread initialize(JobStatSyncConfig config, LoaderServerClient serverClient) {
        if(jobStatsSyncThread == null) {
            jobStatsSyncThread = new JobStatsSyncThread(config, serverClient);
            jobStatsSyncThread.start();
        }
        return jobStatsSyncThread;
    }

    public static JobStatsSyncThread getInstance() {
        return jobStatsSyncThread;
    }

    private JobStatsSyncThread(JobStatSyncConfig config, LoaderServerClient serverClient) {
        this.syncConfig = config;
        this.jobIds = new ArrayList<String>();
        this.serverClient = serverClient;
    }

    public void addJobToSync(String jobId) {
        synchronized (jobIds) {
            this.jobIds.add(jobId);
        }
    }

    public void removeJob(String jobId) {
        synchronized (jobIds) {
            syncJobStatFiles(jobId);
            this.jobIds.remove(jobId);
        }
    }

    public void run() {
        while(true) {
            synchronized (jobIds) {
                for(String jobId : jobIds) {
                    syncJobStatFiles(jobId);
                }
            }
            System.out.println("Sleeping for :"+this.syncConfig.getSyncInterval()+"ms");
            Clock.sleep(this.syncConfig.getSyncInterval());
        }

    }

    private void syncJobStatFiles(String jobId) {
        String jobPath = syncConfig.getJobBasePath() + File.separator + jobId;

        List<File> jobFiles = FileHelper.pathFiles(jobPath, true);
        Collections.sort(jobFiles);
        log.info("Job "+jobId+" Files to Read and may have to publish "+jobFiles.size());
        for(File jobFile : jobFiles) {
            if(jobFile.getAbsolutePath().endsWith("done"))
                publishAndDelete(jobId, jobFile);
        }
    }

    private void publishAndDelete(String jobId, File jobFile) {
        try {
            if(jobFile.length() > 0)
                this.serverClient.publishJobStats(jobId,
                    jobFile.getAbsolutePath(),
                    trimFileName(jobFile.getAbsolutePath()));
            jobFile.delete();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private String trimFileName(String absoluteFileName) {
        String trimmedFileName = absoluteFileName.replace(syncConfig.getJobBasePath(), "");
        trimmedFileName = trimmedFileName.substring(0, trimmedFileName.indexOf(".part"));
        return trimmedFileName;
    }
}
