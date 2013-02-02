package loader.monitor.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 3/10/12
 * Time: 3:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileHelper {
    public static String fileContent(String filePath) throws IOException {
        return streamContent(new FileInputStream(filePath));
    }

    public static String streamContent(InputStream is) throws IOException {
        String fileContent = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = br.readLine()) != null) {
            fileContent += line + "\n";
        }
        br.close();
        return fileContent.trim();
    }

    public static void appendToFile(String filePath, String content) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true)));
            bw.write(content + "\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            bw.close();
        }
    }

    public static Collection<? extends String> getLinesFromFile(String file) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0)
                    lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            br.close();
        }
        return lines;
    }

    public static void flushLinesToFile(List<String> lines, String file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        try {
            for (String line : lines) {
                bw.write(line + "\n");
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            bw.close();
        }
    }

}
