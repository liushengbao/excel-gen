package org.util;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shengbao.Liu
 * @email 646406929@qq.com
 */
public class FileUtil {


    /**
     * 读取文本文件内容到字符串
     *
     * @param absPath
     * @return the string represent file content
     */
    public static String readFileContent(String absPath) {
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(absPath));
            String str = null;
            while ((str = reader.readLine()) != null) {
                result.append(str).append("\r\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }

    /**
     * 写入字符串文件内容，这个操作是覆盖型操作
     *
     * @param absPath
     * @param content
     */
    public static void writeFile(String absPath, String content) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(absPath)));
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeFile(File file, String content) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<File> listFiles(String path) {
        List<File> list = new ArrayList<>();
        File file = new File(path);
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                list.addAll(listFiles(path + f.getName() + File.separator));
            } else if (f.isFile()) {
                list.add(f);
            }
        }
        return list;
    }

    public static void main(String[] args) {
        List<File> list = listFiles("E:\\project\\runtime24\\配置文件\\策划配置文件\\");
        list.forEach(v -> System.out.println(v.getName()));
    }

    public static void checkCreateDir(String path) {
        File jsonDirFile = new File(path);
        if (!jsonDirFile.exists()) {
            try {
                jsonDirFile.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
