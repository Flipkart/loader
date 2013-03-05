package com.open.perf.example;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 4/3/13
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class FlushFile extends Thread{
    private BufferedWriter bw;
    private long done = 0;

    public FlushFile(String file) throws FileNotFoundException {
        this.bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
    }

    public void run() {
        long totalTime = 0;
        long count = 10 * 10 * 100000;
        long baseStartTime = System.nanoTime();
        while(count > 0) {
            long startTime = System.nanoTime();
            try {
                bw.write("Hello How Are youHello How Are\n");
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            totalTime += (System.nanoTime() - startTime)/1000;
            count--;
            done++;
            if(done%1000 == 0) {
                try {
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                System.out.println("Time for Last 1000 requests :"+totalTime);
                totalTime = 0;
            }

        }

        System.out.println("Time take for 50m write requests :"+(System.nanoTime() - baseStartTime)/1000 +"Micro s");
    }

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
/*
        FlushFile ff = new FlushFile("/tmp/ff.txt");
        ff.start();
        ff.join();
*/
        for(int i=0;i<100;i++){
            System.out.println(System.nanoTime());
        }
    }

}
