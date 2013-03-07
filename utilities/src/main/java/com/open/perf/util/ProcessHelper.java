package com.open.perf.util;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 3/1/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessHelper {

    public static String getOutput(Process process) throws IOException {
        return StreamHelper.inputStreamContent(process.getInputStream());
    }

    public static String getError(Process process) throws IOException {
        return StreamHelper.inputStreamContent(process.getErrorStream());
    }

    public static boolean wait(final Process process, final int maxWait) throws InterruptedException {
        class ProcessWait extends Thread {
            private boolean processOver = true;
            public void run() {
                int totalDelay = 0;
                while(totalDelay < maxWait) {
                    try {
                        if(ProcessHelper.processOver(process))
                            break;
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    totalDelay += 100;
                    if(totalDelay >= maxWait) {
                        break;
                    }
                }

                this.processOver = ProcessHelper.processOver(process);
            }
        }
        ProcessWait pWait = new ProcessWait();
        pWait.start();
        pWait.join();

        return pWait.processOver;
    }

    public static boolean processOver(Process process){
        try{
            process.exitValue();
            return true;
        }
        catch(IllegalThreadStateException e) {
            return false;
        }
    }
}

