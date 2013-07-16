package com.open.perf.core;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import com.open.perf.domain.Load;
import com.open.perf.jackson.ObjectMapperUtil;
import com.open.perf.operation.MathAddFunction;
import com.open.perf.operation.PrintParameters;
import com.open.perf.util.Clock;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 26/6/13
 * Time: 10:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceInputFileTest {
    @Test
    public void test() throws Exception {
//        Load load = ObjectMapperUtil.instance().readValue(new File("/tmp/1373969986019"), Load.class);
//        load.start(""+ Clock.milliTick());

        Load load = new Load().
                addGroup(new Group().
                        setName("G1").
                        setRepeats(1).
                        addFunction(new GroupFunction().
                                setFunctionClass(PrintParameters.class.getCanonicalName()).
                                setFunctionalityName("Print Parameters").
                                addParam("parameters", "[\"someParameter\"]").
                                addParam("someParameter", "${urlsTohit}")
                        ));
        load.start(""+System.currentTimeMillis());
    }
}
