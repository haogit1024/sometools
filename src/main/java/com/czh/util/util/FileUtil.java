package com.czh.util.util;

import org.apache.commons.lang3.StringUtils;

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
        if (StringUtils.isBlank(filePath)) {
            throw new IOException("文件路径不能为空");
        }
        this.filePath = filePath;
        File file = new File(filePath);
        // 如果存在文件, 先删除文件
        if (file.exists()) {
            boolean deleteRes = file.delete();
            if (!deleteRes) {
                throw new IOException("删除文件失败, 文件路径: " + filePath);
            }
        }
        String dirPath = filePath.substring(0, filePath.lastIndexOf(File.separator));
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean createRes = dir.mkdirs();
            if (!createRes) {
                throw new IOException("创建缓存目录失败, 文件路径: " + filePath);
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
        String path = "C:\\Users\\czh\\Desktop\\作品\\test.sql";
        int index = path.lastIndexOf("\\");
        System.out.println(index);
        System.out.println(path.substring(0, index));
        System.out.println(File.separator);
    }
}
