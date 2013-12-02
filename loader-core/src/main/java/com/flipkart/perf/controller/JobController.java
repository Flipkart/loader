package com.flipkart.perf.controller;

import com.strategicgains.restexpress.Request;
import com.strategicgains.restexpress.Response;

import java.util.List;
import java.util.Map;

public class JobController {

    /**
     * Kill the job abruptly
     * @param request
     * @param response
     */
    public void kill(Request request, Response response) {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();
    }
}
