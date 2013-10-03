package com.flipkart.perf.server.dataFix;

import com.flipkart.perf.server.config.LoaderServerConfiguration;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 26/6/13
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DataFixer {
    public boolean fix(LoaderServerConfiguration configuration);
}
