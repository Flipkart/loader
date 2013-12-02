package com.flipkart.perf.sample.search;

import org.apache.commons.pool.BasePoolableObjectFactory;

public class SearchFactory extends BasePoolableObjectFactory<Search>{
    @Override
    public Search makeObject() throws Exception {
        return new Search();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
