package server.monitor;

import com.open.perf.util.Clock;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 15/5/13
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoopProcess {
    public static void main(String[] args) throws InterruptedException {
        while(true) {
            Clock.sleep(10000);
        }
    }
}
