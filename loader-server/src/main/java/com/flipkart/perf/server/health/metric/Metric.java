package com.flipkart.perf.server.health.metric;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 24/10/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Metric {
    public static enum MetricType{
        /**
         * Is for things like temperatures or number of people in a room or the value of a RedHat share.
         */
        GAUGE,

        /**
         * Is for continuous incrementing counters like the ifInOctets counter in a router.
         */
        COUNTER,

        /**
         * Will store the derivative of the line going from the last to the current value of the data source.
         */
        DERIVE,

        /**
         * Is for counters which get reset upon reading.
         */
        ABSOLUTE
    }

    private String name;
    private Double value;
    private MetricType metricType = MetricType.GAUGE;

    public Metric(String name, Double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public Metric setMetricType(MetricType metricType) {
        this.metricType = metricType;
        return this;
    }
}
