package loader.monitor.domain;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 23/1/13
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class Metric {
    private String name;
    private Double value;

    public String getName() {
        return name;
    }

    public Metric setName(String name) {
        this.name = name;
        return this;
    }

    public Double getValue() {
        return value;
    }

    public Metric setValue(Double value) {
        this.value = value;
        return this;
    }

    public String toString() {
        return "Name :"+name+" Value :"+value;
    }
}
