package perf.operation.http;

import com.flipkart.perf.main.Main;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 20/12/13
 * Time: 8:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleRun {
    public static void main(String[] args) throws Exception {
        Main.main(new String[] {
                "-f", "/tmp/1387551125546",
                "-j", System.currentTimeMillis()+"",
                "-s", "/var/log/loader-agent/jobs/ ",
                "-p", "4567"
        });

    }
}
