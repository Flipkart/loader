package com.open.perf.util;

import java.io.*;

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
            e.printStackTrace();
            throw e;
        }
        finally {
            bufferedReader.close();
        }

        return content.toString();
    }

    public static void readAndWrite(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;

        while((bytesRead = is.read(buffer)) > 0) {
            os.write(buffer, 0, bytesRead);
            os.flush();
        }
    }

    public static void readAndWrite(InputStream inputStream, PrintWriter writer) throws IOException {
        BufferedWriter bw = new BufferedWriter(writer);
        InputStreamReader reader = new InputStreamReader(inputStream);
        char[] buffer = new char[4096];
        int charsRead;

        while((charsRead = reader.read(buffer)) > 0) {
            writer.write(buffer, 0, charsRead);
            writer.flush();
        }
    }
}
