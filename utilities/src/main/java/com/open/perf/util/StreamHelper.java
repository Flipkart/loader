package com.open.perf.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 3/1/13
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class StreamHelper {
    public static String inputStreamContent(InputStream is) throws IOException {
        StringBuilder content = new StringBuilder("");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        char[] buffer = new char[4096];
        int charsRead;

        try {
            while((charsRead = bufferedReader.read(buffer)) > 0) {
                content.append(buffer,0,charsRead);
            }
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            bufferedReader.close();
        }

        return content.toString();
    }
}
