package com.czh.util.database.base;

import java.io.*;
import java.util.List;

/**
 * @author czh
 * 操作文件类
 */
public class FileUtil {
    private String filePath;
    private FileOutputStream fos;

    public FileUtil(String filePath) throws IOException {
        this.filePath = filePath;
        File file = new File(filePath);
        createFile(file);
    }

    public FileUtil(String dir, String fileName) throws IOException {
        File file = new File(dir, fileName);
        this.filePath = file.getPath();
        createFile(file);
    }

    private void createFile(File file) throws IOException {
        if (file.exists()) {
            boolean b = file.delete();
            if (!b) {
                throw new IOException("删除文件失败 文件路径: " + filePath);
            }
        }
        fos = new FileOutputStream(file, true);
    }

    /**
     * 向文件添加文本
     * @param str 需要添加的文本
     */
    public void append(String str) {
        try {
            fos.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("写入文件失败, filePath: %s, content: %s \n", this.filePath, str);
            System.exit(0);
        }
    }

    /**
     * 向文件添加多行文本
     * @param list
     * @throws IOException
     */
    public void append(List<String> list) throws IOException {
        // 需要再实现
    }

    /**
     * 关闭所有流
     */
    public void close() {
        if (fos != null) {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("关闭文件出错, 文件名: " + this.filePath);
                System.exit(0);
            }
        }
    }

    public static void main(String[] args) {
//        File file = new File("C:\\Users\\czh\\Desktop\\作品\\test.sql");

    }
}
