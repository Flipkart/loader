package com.flipkart.perf.sample.search;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 3/7/13
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchService {
    private ObjectPool<Search> searchObjectPool;
    public SearchService(int poolSize) {
        searchObjectPool = new GenericObjectPool<Search>(new SearchFactory(), poolSize);
    }

    public List<String> search(String pattern, int delay) throws Exception {
        Search search = null;
        try {
            search = this.searchObjectPool.borrowObject();
            return search.search(pattern, delay);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if(search != null)
                searchObjectPool.returnObject(search);
        }
        return Arrays.asList(new String[]{});
    }
}
