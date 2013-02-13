package perf.agent.daemon;

import org.apache.log4j.Logger;
import perf.agent.client.LoaderServerClient;
import perf.agent.config.JobStatSyncConfig;
import perf.agent.config.ServerInfo;
import perf.agent.util.FileHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 28/1/13
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatSyncThread extends Thread{
    private List<String> jobIds;
    private Map<String,FileTouchPoint> fileTouchPointMap;
    private JobStatSyncConfig syncConfig;
    private static StatSyncThread statSyncThread;
    private ServerInfo serverInfo;
    private static Logger log = Logger.getLogger(StatSyncThread.class);
    private LoaderServerClient serverClient;

    private static class FileTouchPoint {
        private long lastModifiedTime;
        private long lastReadPoint;
        private boolean eofReached;

        private FileTouchPoint(long lastModifiedTime, long lastReadPoint, boolean eofReached) {
            this.lastModifiedTime = lastModifiedTime;
            this.lastReadPoint = lastReadPoint;
            this.eofReached = eofReached;
        }

        public boolean shouldReadFile(long newModifiedTime) {
            return ((this.lastModifiedTime != newModifiedTime) || !this.eofReached) ;
        }
    }

    public static StatSyncThread initialize(JobStatSyncConfig config, LoaderServerClient serverClient) {
        if(statSyncThread == null) {
            statSyncThread = new StatSyncThread(config, serverClient);
            statSyncThread.start();
        }
        return statSyncThread;
    }

    public static StatSyncThread getInstance() {
        return statSyncThread;
    }

    private StatSyncThread(JobStatSyncConfig config, LoaderServerClient serverClient) {
        this.syncConfig = config;
        this.fileTouchPointMap = new HashMap<String, FileTouchPoint>();
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

            try {
                Thread.sleep(this.syncConfig.getSyncInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

    private void syncJobStatFiles(String jobId) {
        String jobPath = syncConfig.getJobBasePath() + File.separator + jobId;

        List<File> jobFiles = FileHelper.pathFiles(jobPath, true);
        //log.debug("Job "+jobId+" Files to Read and may have to publish "+jobFiles.size());
        for(File jobFile : jobFiles) {
            readAndPublish(jobId, jobFile);
        }
    }

    private void readAndPublish(String jobId, File jobFile) {
        FileTouchPoint fileTouchPoint = this.fileTouchPointMap.get(jobFile.getAbsolutePath());

        boolean needToReadFile = false;
        long lastReadPoint = -1;
        if(fileTouchPoint == null) {
            needToReadFile = true;
        }
        else if(fileTouchPoint.shouldReadFile(jobFile.lastModified())) {
            needToReadFile = true;
            lastReadPoint = fileTouchPoint.lastReadPoint;
        }

        //log.debug("Job "+jobId+" file "+jobFile.getAbsolutePath()+" Need to Read :"+needToReadFile);

        if(needToReadFile) {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(jobFile, "r");

                if(lastReadPoint != -1)
                    raf.seek(lastReadPoint);

                String linesRead = "";
                String line = null;
                boolean eofReached = false;
                for(int i=0;i<syncConfig.getLinesToSyncInOneGo();i++) {
                    if((line = raf.readLine()) != null) {
                        linesRead += line +"\n";
                    }
                    else {
                        eofReached = true;
                        break;
                    }
                }
                lastReadPoint = raf.getFilePointer();
                this.serverClient.publishJobStats(jobId,
                                        jobFile.getAbsolutePath().replace(syncConfig.getJobBasePath(), ""),
                                        linesRead);

                fileTouchPointMap.put(jobFile.getAbsolutePath(), new FileTouchPoint(jobFile.lastModified(), lastReadPoint, eofReached));

            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ExecutionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } finally {
                if(raf != null)
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
            }

        }
    }
}
