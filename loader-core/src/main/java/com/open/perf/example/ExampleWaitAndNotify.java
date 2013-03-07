package com.open.perf.example;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import javax.xml.ws.AsyncHandler;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 17/2/13
 * Time: 8:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExampleWaitAndNotify extends Thread{

    private AsyncHttpClient client = new AsyncHttpClient();
    private List<AsyncHttpClient.BoundRequestBuilder> requestBuilders;
    private MyHandler myHandler;
    private Object obj = new Object();
    public ExampleWaitAndNotify() {
        requestBuilders = new ArrayList<AsyncHttpClient.BoundRequestBuilder>();
        for(int i=0;i<10000;i++) {
            requestBuilders.add(client.prepareGet("http://localhost:9999/loader-server/delay?time=1"));
        }
        myHandler = new MyHandler(obj);
    }

    public void run() {

        long startTime = System.currentTimeMillis();
        for(AsyncHttpClient.BoundRequestBuilder b : requestBuilders) {
            try {
                System.out.println(System.currentTimeMillis()+" Executing Request");
                long requestStartTime = System.currentTimeMillis();
                Future<Response> r =  b.execute(myHandler);
                System.out.println(System.currentTimeMillis()+" Going To Wait");
                synchronized (obj){
                    obj.wait();
                }
                System.out.println(System.currentTimeMillis()+" Request State :"+r.isDone());
                System.out.println(System.currentTimeMillis()+" Request Response :"+r.get().getResponseBody());
                System.out.println((System.currentTimeMillis()- requestStartTime));
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ExecutionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        System.out.println(System.currentTimeMillis() - startTime);
        this.client.close();
    }

    public static void main(String[] args) throws InterruptedException {
        ExampleWaitAndNotify t = new ExampleWaitAndNotify();
        t.start();
//        t.join();
    }
}


class MyHandler extends AsyncCompletionHandler<Response> {
    private final Object waitNotifyThread;

    public MyHandler(Object waitNotifyThread) {
        this.waitNotifyThread = waitNotifyThread;
    }

    @Override
    public Response onCompleted(Response response) throws Exception {
        System.out.println(System.currentTimeMillis()+" Notifying the thread");
        synchronized (this.waitNotifyThread){
            this.waitNotifyThread.notify();
        }
        return response;
    }
}

