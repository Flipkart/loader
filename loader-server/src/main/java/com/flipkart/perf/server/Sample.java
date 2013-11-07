package com.flipkart.perf.server;
import java.util.concurrent.*;

public class Sample {
    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(5);

        ScheduledFuture scheduledFuture =
                scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Executed!");
//                        throw new RuntimeException("Error");
                    }
                },      1,
                        1,
                        TimeUnit.SECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            int counter = 1;

            @Override
            public void run() {
                System.out.println(Thread.currentThread() + " Executed2! "+counter++);
//                        throw new RuntimeException("Error");
            }
        },      1,
                1,
                TimeUnit.SECONDS);
/*
        scheduledExecutorService.schedule(new Callable() {
            public Object call() throws Exception {
                System.out.println("Executed!");
                return "Called!";
            }
        },
                5,
                TimeUnit.SECONDS);
*/
    }
}

