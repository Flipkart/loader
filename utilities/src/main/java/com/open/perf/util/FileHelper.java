package com.open.perf.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper to do various file operations
 */
public class FileHelper {
    public static String persistStream(InputStream libStream, String targetFile) throws IOException {
        return persistStream(libStream, targetFile, false);
    }

    public static String persistStream(InputStream libStream, String targetFile, boolean append) throws IOException {
        byte[] buffer = new byte[8024];
        createFile(targetFile);
        BufferedInputStream bis = new BufferedInputStream(libStream);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile, append));

        int bytesRead;
        while((bytesRead = bis.read(buffer)) > 0) {
            bos.write(buffer,0,bytesRead);
        }
        bos.flush();
        bis.close();
        bos.close();
        return targetFile;
    }

    synchronized public static void mergeMappingFile(String libPath, InputStream classListInputStream, String mappingFile) throws IOException {
        Properties prop = new Properties();
        createFile(mappingFile);
        InputStream mappingFileIS = new FileInputStream(mappingFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(classListInputStream));
        try {
            createFile(mappingFile);
            prop.load(mappingFileIS);

            String className;
            while((className = br.readLine()) != null) {
                if(!className.trim().equals(""))
                    prop.put(className, libPath);
            }

            prop.store(new FileOutputStream(mappingFile), "Class and Library Mapping");

        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            br.close();
            mappingFileIS.close();
        }
    }

    public static void createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if(!file.exists())
            file.createNewFile();
    }

    public static void unzip(InputStream libInputStream, String path) throws IOException {
        createFolder(path);
        ZipInputStream zis = new ZipInputStream(libInputStream);

        try {
            ZipEntry ze;
            byte[] buffer = new byte[4096];

            while ((ze = zis.getNextEntry()) != null){
                if (ze.isDirectory())
                    continue;

                String fileName = ze.getName();

                int bytesRead;
                String filePath = path + File.separator + fileName;
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(filePath);
                    while ((bytesRead = zis.read(buffer)) > 0){
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                catch(IOException ioe) {
                    ioe.printStackTrace();
                } finally {
                    if(fos != null) {
                        fos.flush();
                        fos.close();
                    }
                    zis.closeEntry();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            if (zis != null)
                zis.close();
        }
    }

    private static void createFolder(String path) {
        File file = new File(path);
        if(!file.exists())
            file.mkdirs();

    }

    public static void rename(String platformLibPath, String newPath) {
        new File(platformLibPath).renameTo(new File(newPath));
    }

    public static void remove(String path) {
        File pathHandle = new File(path);
        if(pathHandle.isDirectory()) {
            File[] files = pathHandle.listFiles();
            for(File file : files) {
                if(file.isDirectory())
                    remove(file.getAbsolutePath());
                file.delete();
            }
            pathHandle.delete();
        }
        else
            pathHandle.delete();
    }

    public static List<File> pathFiles(String path, boolean recursively) {
        List<File> allFiles = new ArrayList<File>();
        File jobPathObj = new File(path);
        File[] pathFiles = jobPathObj.listFiles();
        if(pathFiles != null) {
            for(File pathFile : pathFiles) {
                if(pathFile.isDirectory()) {
                    if(recursively)
                        allFiles.addAll(pathFiles(pathFile.getAbsolutePath(), recursively));
                }
                else
                    allFiles.add(pathFile);
            }
        }
        return allFiles;
    }

    // Chane it to use Byte data instead of line. Would work for binary data as well
    public static String readContent(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuffer content = new StringBuffer();
        String line = null;
        try {
            while((line = br.readLine()) != null)
                content.append(line+"\n");
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            br.close();
        }
        return content.toString();
    }

    public static void move(String src, String target) {
        File srcFile = new File(src);
        if(srcFile.exists())
            srcFile.renameTo(new File(target));
    }

    /**
     * Assumption is the input path contains the file name aswell.
     * @param statFilePath
     */
    public static void createFilePath(String statFilePath) {
        String parentPath = new File(statFilePath).getParent();
        createFolder(parentPath);
    }
}
