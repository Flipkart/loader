package perf.agent.daemon;

import com.open.perf.util.Clock;
import com.open.perf.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.agent.client.LoaderServerClient;
import perf.agent.config.JobFSConfig;
import perf.agent.config.JobStatSyncConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Agent Thread which is responsible for pushing all job stats back to server
 */
public class JobStatsSyncThread extends Thread{
    private List<String> jobIds;
    private JobStatSyncConfig syncConfig;
    private static JobStatsSyncThread jobStatsSyncThread;
    private static Logger logger = LoggerFactory.getLogger(JobStatsSyncThread.class);
    private LoaderServerClient serverClient;
    private final JobFSConfig jobFSConfig;

    public static JobStatsSyncThread initialize(JobStatSyncConfig config,
                                                JobFSConfig jobFSConfig,
                                                LoaderServerClient serverClient) {
        if(jobStatsSyncThread == null) {
            jobStatsSyncThread = new JobStatsSyncThread(config, jobFSConfig, serverClient);
            jobStatsSyncThread.start();
        }
        return jobStatsSyncThread;
    }

    public static JobStatsSyncThread instance() {
        return jobStatsSyncThread;
    }

    private JobStatsSyncThread(JobStatSyncConfig config, JobFSConfig jobFSConfig, LoaderServerClient serverClient) {
        this.syncConfig = config;
        this.jobIds = new ArrayList<String>();
        this.jobFSConfig = jobFSConfig;
        this.serverClient = serverClient;
    }

    public void addJob(String jobId) {
        synchronized (jobIds) {
            this.jobIds.add(jobId);
        }
    }

    public void removeJob(String jobId) {
        synchronized (jobIds) {
            if(jobIds.contains(jobId)) {
                syncJobStatFiles(jobId);
                this.jobIds.remove(jobId);
            }
        }
    }

    public void run() {
        while(true) {
            synchronized (jobIds) {
                for(String jobId : jobIds) {
                    syncJobStatFiles(jobId);
                }
            }
            try {
                Clock.sleep(this.syncConfig.getSyncInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void syncJobStatFiles(String jobId) {
        String jobPath = jobFSConfig.getJobPath(jobId);

        List<File> jobFiles = FileHelper.pathFiles(jobPath, true);
        Collections.sort(jobFiles);
        logger.info("Job "+jobId+" Files to Read and may have to publish "+jobFiles.size());
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
        String trimmedFileName = absoluteFileName.replace(jobFSConfig.getJobBasePath(), "");
        trimmedFileName = trimmedFileName.substring(0, trimmedFileName.indexOf(".part"));
        return trimmedFileName;
    }
}
