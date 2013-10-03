package com.flipkart.perf.sample.search;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
    }

    public static void loadNames(String fileContainingNames) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileContainingNames)));
        String name = null;
        while((name = br.readLine()) != null) {
            names.add(name.trim());
        }
    }

    public List<String> search(String pattern, int delay) {
        if(delay > 0 ) {
            try {
                System.out.println("Sleeping for "+delay + " ms");
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        List<String> matchingNames = new ArrayList<String>();
        for(String name : names) {
            if(name.toUpperCase().contains(pattern.toUpperCase()))
                matchingNames.add(name);
        }
        return matchingNames;
    }
}
