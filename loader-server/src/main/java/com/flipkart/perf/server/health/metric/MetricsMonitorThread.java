package com.flipkart.perf.server.health.metric;

import com.flipkart.perf.common.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/10/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetricsMonitorThread extends Thread{

    private boolean keepRunning = true;
    private int interval = 1000;
    private List<MetricMonitor> metricMonitors;
    private Map<String, Long> monitorLastExecutionTime;
    private static Logger logger = LoggerFactory.getLogger(MetricsMonitorThread.class);
    private static MetricsMonitorThread instance;

    private  MetricsMonitorThread(List<MetricMonitor> metricMonitors) {
        this.metricMonitors = metricMonitors;
        monitorLastExecutionTime = new HashMap<String, Long>();

    }

    public static void startMonitoring(List<MetricMonitor> metricMonitors) {
        if(instance == null) {
            instance = new MetricsMonitorThread(metricMonitors);
            instance.start();
        }
    }

    public void run() {
        while(keepRunning) {
            for(MetricMonitor metricMonitor : metricMonitors) {
                if(canMetricMonitorRun(metricMonitor)) {
                    try {
                        logger.info("Collecting Metric for "+metricMonitor.getName()+ " monitor");
                        MetricArchiverQueue.offer(metricMonitor.get());
                    }
                    catch (Exception e) {
                        logger.error("Error while running "+metricMonitor.getName() + " Monitor", e);
                    }
                    finally {
                        this.monitorLastExecutionTime.put(metricMonitor.getName(), Clock.milliTick());
                    }
                }
            }
            try {
                Clock.sleep(this.interval);
            } catch (InterruptedException e) {
                logger.error("Error while sleeping", e);
            }
        }
    }

    private boolean canMetricMonitorRun(MetricMonitor metricMonitor) {
        Long lastExecutionTime =  this.monitorLastExecutionTime.get(metricMonitor.getName());
        if(lastExecutionTime == null) {
            return true;
        }
        else {
            return (Clock.milliTick() - lastExecutionTime) > metricMonitor.getInterval();
        }
    }

    public static void stopMonitoring() {
        instance.keepRunning = false;
    }
}