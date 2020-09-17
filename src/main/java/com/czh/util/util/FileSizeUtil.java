package com.czh.util.util;

import com.czh.util.orm.ORMDataBase;
import com.czh.util.orm.entity.FileSize;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author chenzh
 * @date 2020/9/17
 */
public class FileSizeUtil {
    private static final ORMDataBase orm = new ORMDataBase("db.properties");
    private static final String FILE_SYSTEM = "tc-win";
    private static final Integer SCAN_TIME = (int)(new Date().getTime() / 1000);
    private static final ExecutorService executor = Executors.newFixedThreadPool(5000);

    public static long getSizeFromDir(String dirPath) {
        return getSizeFromDir(new File(dirPath));
    }

    public static long getSizeFromDir(File dirFile) {
        if (dirFile == null || !dirFile.exists()) {
            return -1;
        }
        File[] tempFiles = dirFile.listFiles();
        if (tempFiles == null || tempFiles.length == 0) {
            return 0;
        }
        long ret = 0;
        for (File file : tempFiles) {
            long size;
            int isDir;
            if (file.isDirectory()) {
                size = getSizeFromDir(file);
                isDir = 1;
            } else {
                size = file.length();
                isDir = 0;
            }
//            System.out.println("size: " + size);
            ret+=size;
            final FileSize fileSize = new FileSize(FILE_SYSTEM, file.getParent(), file.getAbsolutePath(), file.getName(), size, isDir, SCAN_TIME);
            executor.submit(() -> saveOrUpdate(fileSize));
        }
        return ret;
    }

    public static void saveOrUpdate(FileSize fileSize) {
        try {
            FileSize selectCondition = new FileSize();
            selectCondition.setFileSystem(FILE_SYSTEM).setFilePath(fileSize.getFilePath());
            FileSize oldFileSize = orm.selectOne(selectCondition);
            int ret;
            if (oldFileSize == null) {
                // insert
                ret = orm.insert(fileSize);
            } else {
                // update
                ret = orm.update(fileSize, "file_system", "file_path");
            }
//            System.out.println("ret: " + ret);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        long ret = getSizeFromDir("D:\\tc_codes\\etc-pay-platform\\app-api\\target");
        long startTime = System.currentTimeMillis();
        long ret = getSizeFromDir("D:\\tc_codes");
        System.out.println(ret);
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("保存结束");
        System.out.println("耗时: " + ((endTime - startTime) / 1000) + " 秒");
//        orm.close();
    }
}
