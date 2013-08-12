package perf.server.dataFix;

import com.open.perf.util.ClassHelper;
import org.codehaus.jackson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.server.config.DataFixConfig;
import perf.server.config.LoaderServerConfiguration;
import perf.server.util.ObjectMapperUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 30/6/13
 * Time: 6:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataFixRunner {
    private final DataFixConfig dataFixConfig;
    private static Logger logger = LoggerFactory.getLogger(DataFixRunner.class);

    public DataFixRunner(DataFixConfig dataFixConfig) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.dataFixConfig = dataFixConfig;
    }

    public void run() throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        List<String> fixerClasses = ObjectMapperUtil.instance().readValue(new File(dataFixConfig.getDataFixersFile()), List.class);
        List<String> fixersDone = ObjectMapperUtil.instance().readValue(new File(dataFixConfig.getDoneFixersFile()), List.class);
        for(String fixerClass : fixerClasses) {
            if(!fixersDone.contains(fixerClass)) {
                DataFixer dataFixer = (DataFixer) ClassHelper.getClassInstance(fixerClass, new Class[]{}, new Object[]{});
                logger.info("Running Data Fixer '"+fixerClass+"'");
                boolean fixed = dataFixer.fix(LoaderServerConfiguration.instance());
                logger.info("Data Fixer '"+fixerClass+"' fixed ? "+fixed);

                if(fixed) {
                    fixersDone.add(fixerClass);
                }
            }
            else {
                logger.info("Data Fixer '"+fixerClass+"'Already finished");
            }
        }
        ObjectMapperUtil.instance().writerWithDefaultPrettyPrinter().writeValue(new File(dataFixConfig.getDoneFixersFile()), fixersDone);
    }
}
