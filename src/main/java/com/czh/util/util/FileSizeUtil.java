package com.czh.util.util;

import com.czh.util.orm.ORMDataBase;
import com.czh.util.orm.entity.FileSize;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author chenzh
 * @date 2020/9/17
 */
public class FileSizeUtil {
    private static final ORMDataBase orm = new ORMDataBase("mydb.properties");
    private static final String FILE_SYSTEM = "tc-win";
    private static final Integer SCAN_TIME = (int)(System.currentTimeMillis() / 1000);
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2000);
    private static int fileNum = 0;

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
            fileNum++;
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
            EXECUTOR.submit(() -> saveOrUpdate(fileSize));
            /*new Thread(() -> {
                saveOrUpdate(fileSize);
            }).start();*/
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
        orm.setPower(true);
        long startTime = System.currentTimeMillis();
        long ret = getSizeFromDir("D:\\tc_codes");
//        long ret = getSizeFromDir("C:\\Users\\admin");
        long scanEndTime = System.currentTimeMillis();
//        long ret = getSizeFromDir("D:\\czhcode\\github\\java\\simple");
        System.out.println(ret);
        System.out.println(fileNum);
        System.out.println("扫描耗时: " + ((scanEndTime - startTime) / 1000) + " 秒");
        EXECUTOR.shutdown();
        try {
            EXECUTOR.awaitTermination(10, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long saveEndTime = System.currentTimeMillis();
        System.out.println("保存结束");
        System.out.println("耗时: " + ((saveEndTime - startTime) / 1000) + " 秒");
//        orm.close();
//        try {
//            Thread.sleep(1000 * 60 * 60);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
