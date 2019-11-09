package com.czh.util.database.base;

import java.io.*;
import java.util.List;

/**
 * @author czh
 * 操作文件类
 */
public class FileUtil {
    private String fileName;
    private FileOutputStream fos;

    public FileUtil(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
        fos = new FileOutputStream(fileName);
    }

    /**
     * 向文件添加文本
     * @param str 需要添加的文本
     * @throws IOException
     */
    public void append(String str) throws IOException {
        fos.write(str.getBytes());
    }

    /**
     * 向文件添加多行文本
     * @param list
     * @throws IOException
     */
    public void append(List<String> list) throws IOException {
        // 需要再实现
    }

    public void close() {
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("关闭文件出错, 文件名: " + this.fileName);
            }
        }
    }

    public static void main(String[] args) {
        File file = new File("");
    }
}
