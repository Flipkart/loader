package perf.sample.config;

import com.yammer.dropwizard.config.Configuration;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 2/7/13
 * Time: 6:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleServiceConfiguration extends Configuration {
    private String name;
    private int searchPoolSize;
    private int cacheSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSearchPoolSize() {
        return searchPoolSize;
    }

    public void setSearchPoolSize(int searchPoolSize) {
        this.searchPoolSize = searchPoolSize;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
}
