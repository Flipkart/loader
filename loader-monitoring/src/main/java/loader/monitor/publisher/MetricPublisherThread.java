package loader.monitor.publisher;

import loader.monitor.domain.MetricPublisherRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 24/1/13
 * Time: 4:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetricPublisherThread extends Thread{
    private static final Logger log = Logger.getLogger(MetricPublisherThread.class);
    private Map<String,MetricPublisherRequest> requestPublisherMap;
    private Map<String,Long> requestLastExecutionTimeMap;
    private int interval;

    public MetricPublisherThread(int interval) {
        this.interval = interval;
        this.requestPublisherMap = new HashMap<String, MetricPublisherRequest>();
        this.requestLastExecutionTimeMap = new HashMap<String, Long>();
    }

    public void run() {
        while(true) {
            for(MetricPublisherRequest metricPublisherRequest : requestPublisherMap.values()) {
                if(!metricPublisherRequest.requestExpired() &&
                        canPublisherRun(metricPublisherRequest)) {
                    try {
                        metricPublisherRequest.publish();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (ExecutionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } finally {
                        this.requestLastExecutionTimeMap.put(metricPublisherRequest.getRequestId(),System.currentTimeMillis());
                    }

                }
            }
            try {
                Thread.sleep(this.interval);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void addRequest(MetricPublisherRequest metricPublisherRequest) {
        synchronized (metricPublisherRequest) {
            this.requestPublisherMap.put(metricPublisherRequest.getRequestId(), metricPublisherRequest);
        }
    }
    public void removeRequest(MetricPublisherRequest metricPublisherRequest) {
        synchronized (metricPublisherRequest) {
            this.requestPublisherMap.remove(metricPublisherRequest.getRequestId());
        }
    }

    private boolean canPublisherRun(MetricPublisherRequest metricPublisherRequest) {
        Long lastExecutionTime =  this.requestLastExecutionTimeMap.get(metricPublisherRequest.getRequestId());
        if(lastExecutionTime != null) {
            //log.debug("Time Passed Last Execution("+ metricPublisherRequest.getRequestId()+") :"+(System.currentTimeMillis() - lastExecutionTime) + "ms");
            return (System.currentTimeMillis() - lastExecutionTime) > metricPublisherRequest.getInterval();
        }
        return true;
    }

}
