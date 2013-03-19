package perf.server.experiment;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RealTimeStatsCalculator {

    public static void main(String[] args) {
        Histogram histogram = Metrics.newHistogram(new MetricName("G1","T1","N1"), true);
        List<Integer> values = new ArrayList<Integer>();
        for(int i=0; i<1000;i++) {
            int randValue = new Random().nextInt(100000);
            values.add(randValue);
            histogram.update(randValue);
            if(i%100 == 0) {
                Collections.sort(values);
                int index = (int)(values.size() * .99f);
                int myValue = values.get(index);
                double error = myValue - histogram.getSnapshot().get99thPercentile();
                double errorPer = (error * 100)/myValue;
                System.out.println(histogram.getSnapshot().get99thPercentile()+","+myValue+","+errorPer);
            }
        }

    }
}
