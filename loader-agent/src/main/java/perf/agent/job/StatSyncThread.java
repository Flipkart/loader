package perf.agent.job;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.apache.log4j.Logger;
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
import java.util.concurrent.Future;

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

    public static StatSyncThread initialize(JobStatSyncConfig config, ServerInfo serverInfo) {
        if(statSyncThread == null) {
            statSyncThread = new StatSyncThread(config, serverInfo);
            statSyncThread.start();
        }
        return statSyncThread;
    }

    public static StatSyncThread getInstance() {
        return statSyncThread;
    }

    private StatSyncThread(JobStatSyncConfig config, ServerInfo serverInfo) {
        this.syncConfig = config;
        this.fileTouchPointMap = new HashMap<String, FileTouchPoint>();
        this.jobIds = new ArrayList<String>();
        this.serverInfo = serverInfo;
    }

    public void addJobToSync(String jobId) {
        this.jobIds.add(jobId);
    }

    public void removeJob(String jobId) {
        this.jobIds.remove(jobId);
    }

    public void run() {
        while(true) {
            synchronized (jobIds) {
                for(String jobId : jobIds) {
                    String jobPath = syncConfig.getJobBasePath() + File.separator + jobId;

                    List<File> jobFiles = FileHelper.pathFiles(jobPath, true);
                    log.info("Job "+jobId+" Files to Read and may have to publish "+jobFiles.size());
                    for(File jobFile : jobFiles) {
                        readAndPublish(jobId, jobFile);
                    }
                }
            }

            try {
                Thread.sleep(this.syncConfig.getSyncInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
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
        log.info("Job "+jobId+" file "+jobFile.getAbsolutePath()+" Need to Read :"+needToReadFile);

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
                publishStatsToServer(jobId,
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

    private void publishStatsToServer(String jobId, String filePath, String linesRead) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        AsyncHttpClient.BoundRequestBuilder b = asyncHttpClient.
                preparePost(this.serverInfo.getBaseUrl() + this.serverInfo.getJobStatsSyncResource().
                        replace("$JOB_ID", jobId).replace("$FILE", filePath)).
                setBody(linesRead);

        Future<Response> r = b.execute();
        r.get();
        asyncHttpClient.close();
    }

    public static void main(String[] args) throws InterruptedException {
        StatSyncThread syncThread = new StatSyncThread(
                new JobStatSyncConfig().
                        setSyncInterval(5000).
                        setLinesToSyncInOneGo(1000).
                        setJobBasePath("/var/log/loader/jobs"),
                new ServerInfo().
                        setBaseUrl("http://localhost:8888/loader-agent/dummy").
                        setJobStatsSyncResource("/jobs/$JOB_ID/stats"));
        syncThread.start();

        syncThread.addJobToSync("job1");
        syncThread.addJobToSync("job2");

        Thread.sleep(60000);
        syncThread.removeJob("job1");
        syncThread.removeJob("job2");

        syncThread.join();
    }
}
