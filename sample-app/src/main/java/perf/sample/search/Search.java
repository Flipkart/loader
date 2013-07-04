package perf.sample.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 3/7/13
 * Time: 5:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class Search {
    private static List<String> names;
    static {
        names = new ArrayList<String>();
        names.add("nitin");
        names.add("rohit");
        names.add("shashank");
        names.add("shwet shashank");
        names.add("swami sir");
        names.add("rahul karmishil");
        names.add("rahul chari");
    }

    public Search() {
    }

    public List<String> search(String pattern) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        List<String> matchingNames = new ArrayList<String>();
        for(String name : names) {
            if(Pattern.matches(pattern, name))
                matchingNames.add(name);
        }
        return matchingNames;
    }
}
