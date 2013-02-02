package loader.monitor.publisher;

import loader.monitor.collector.BaseCollector;
import loader.monitor.domain.PublisherRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 24/1/13
 * Time: 4:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublisherThread extends Thread{
    private static final Logger log = Logger.getLogger(PublisherThread.class);
    private Map<String,PublisherRequest> requestPublisherMap;
    private Map<String,Long> requestLastExecutionTimeMap;
    private int interval;

    public PublisherThread(int interval) {
        this.interval = interval;
        this.requestPublisherMap = new HashMap<String, PublisherRequest>();
        this.requestLastExecutionTimeMap = new HashMap<String, Long>();
    }

    public void run() {
        while(true) {
            for(PublisherRequest publisherRequest : requestPublisherMap.values()) {
                if(!publisherRequest.requestExpired() &&
                        canPublisherRun(publisherRequest)) {
                    try {
                        publisherRequest.publish();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (ExecutionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } finally {
                        this.requestLastExecutionTimeMap.put(publisherRequest.getRequestId(),System.currentTimeMillis());
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

    public void addRequest(PublisherRequest publisherRequest) {
        synchronized (publisherRequest) {
            this.requestPublisherMap.put(publisherRequest.getRequestId(), publisherRequest);
        }
    }
    public void removeRequest(PublisherRequest publisherRequest) {
        synchronized (publisherRequest) {
            this.requestPublisherMap.remove(publisherRequest.getRequestId());
        }
    }

    private boolean canPublisherRun(PublisherRequest publisherRequest) {
        Long lastExecutionTime =  this.requestLastExecutionTimeMap.get(publisherRequest.getRequestId());
        if(lastExecutionTime != null) {
            log.info("Time Passed Last Execution("+publisherRequest.getRequestId()+") :"+(System.currentTimeMillis() - lastExecutionTime) + "ms");
            return (System.currentTimeMillis() - lastExecutionTime) > publisherRequest.getInterval();
        }
        return true;
    }

}
