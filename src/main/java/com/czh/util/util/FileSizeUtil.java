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
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(600);
    private static int fileNum = 0;
    private static final boolean isSaveDb = true;
    private static final RedisUtil redisUtil = new RedisUtil("192.168.20.250", 6379);
    private static final boolean isSaveCache = false;

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
        try {
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
                if (isSaveDb) {
//                    EXECUTOR.submit(() -> saveOrUpdate(fileSize));
                    EXECUTOR.submit(() -> save(fileSize));
//                    EXECUTOR.submit(() -> selectId(fileSize));
                }
                if (isSaveCache) {
                    EXECUTOR.submit(() -> redisUtil.set("file:" + fileSize.getFilePath(), fileSize.toString(), 1000));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("读取文件大小出错啦");
        }
        return ret;
    }

    public static void selectId(FileSize fileSize) {
        FileSize selectCondition = new FileSize();
        selectCondition.setFileSystem(FILE_SYSTEM).setFilePath(fileSize.getFilePath());
        try {
            orm.selectOne(selectCondition);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void save(FileSize fileSize) {
        orm.insert(fileSize);
    }

    public static void saveOrUpdate(FileSize fileSize) {
        try {
            FileSize selectCondition = new FileSize();
            selectCondition.setFileSystem(FILE_SYSTEM).setFilePath(fileSize.getFilePath());
            Integer id = orm.selectId(fileSize);
            int ret;
            if (id == null) {
                // insert
                ret = orm.insert(fileSize);
            } else {
                System.out.println("updateDate.......");
                // update
                ret = orm.update(fileSize, "file_system", "file_path");
            }
//            System.out.println("ret: " + ret);
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        orm.insert(fileSize);
    }

    public static void main(String[] args) {
        if (isSaveDb) {
            orm.setPower(true);
        }
        long startTime = System.currentTimeMillis();
//        long ret = getSizeFromDir("D:\\tc_codes\\etc-pay-platform");
//        long ret = getSizeFromDir("D:\\tc_codes");
        long ret = getSizeFromDir("C:");
        long scanEndTime = System.currentTimeMillis();
//        long ret = getSizeFromDir("D:\\czhcode\\github\\java\\simple");
        System.out.println(ret);
        System.out.println(fileNum);
        System.out.println("扫描耗时: " + ((scanEndTime - startTime) / 1000) + " 秒");
        EXECUTOR.shutdown();
        try {
            EXECUTOR.awaitTermination(100, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long saveEndTime = System.currentTimeMillis();
        System.out.println("保存结束");
        System.out.println("耗时: " + ((saveEndTime - startTime) / 1000) + " 秒");
        orm.close();
        redisUtil.close();
    }
}
