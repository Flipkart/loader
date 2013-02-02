package loader.monitor.util;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 13/9/12
 * Time: 11:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetricsHelper {
    public static <T> void addMetricGauge(Class metricClass, String key, final T value) {
        Metrics.newGauge(metricClass, key, new Gauge<T>() {
            @Override
            public T value() {
                return value;
            }
        });
    }
}
