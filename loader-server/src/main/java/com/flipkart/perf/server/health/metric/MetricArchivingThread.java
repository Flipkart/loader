package com.flipkart.perf.server.health.metric;

import com.flipkart.perf.common.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/10/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetricArchivingThread extends Thread{

    private boolean keepRunning = true;
    private static Logger logger = LoggerFactory.getLogger(MetricArchivingThread.class);
    private final MetricArchivingEngine archivingEngine;
    private static MetricArchivingThread instance;
    private int interval = 1000;

    public MetricArchivingThread(MetricArchivingEngine archivingEngine) {
        this.archivingEngine = archivingEngine;
    }

    public static void startMetricArchiving(MetricArchivingEngine metricArchivingEngine) {
        if(instance == null) {
            instance = new MetricArchivingThread(metricArchivingEngine);
            instance.start();
        }
    }

    public void run() {
        while(keepRunning) {
            try{
                ResourceMetric resourceMetric = MetricArchiverQueue.poll();
                if(resourceMetric == null) {
                    Clock.sleep(interval);
                    continue;
                }
                archivingEngine.archive(resourceMetric);
            }
            catch (Exception e) {
                logger.error("Error while archiving metrics", e);
            }
        }
    }
}


