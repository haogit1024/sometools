package com.czh.util.database;

import com.czh.util.database.base.Database;
import com.czh.util.database.base.FileUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author czh
 * 数据库sql导出类
 */
public class MyDbExport {
    private Database db;
    /**
     * 一次读取的数据量
     */
    private final int saveCount = 5000;

    private final String systemSeparator = System.getProperty("line.separator");

    public MyDbExport(String propertiesPath) {
        db = new Database(propertiesPath);
        printDbInfo();
    }

    public MyDbExport(String host, String port, String username, String password, String database, String driver) {
        db = new Database(host, port, username, password, database, driver);
        printDbInfo();
    }

    private void printDbInfo() {
        System.out.println("表个数: " + db.getTables().size());
        System.out.println("总记录数: " + db.getAllCount());
    }

    private FileUtil createFile(String filePath) {
        FileUtil fileUtil = null;
        try {
            fileUtil = new FileUtil(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("创建导出文件失败, filePath:" + filePath);
        }
        return fileUtil;
    }

    public void exportOneFile(String dirPath, String fileName) {
        String filePath = dirPath + java.io.File.separator + fileName;
        exportOneFile(filePath);
    }

    public void exportOneFile(String filePath) {
        FileUtil fileUtil = createFile(filePath);
        int allCount = db.getAllCount();
        int tempCount = 0;
        String dbHeaderDoc = getDbHeaderDoc();
        // 写入文件头部头部
        fileUtil.append(dbHeaderDoc);
        List<String> tables = db.getTables();
        for (String table: tables) {
            writeTableSql(fileUtil, table);
            tempCount += saveCount;
            System.out.printf("进度: %d%% \n", tempCount / allCount );
        }
        fileUtil.close();
    }

    public void exportMultiFile(String dirPath) {
        db.setPowerMode(true);
        ExecutorService executor = Executors.newFixedThreadPool(Database.threadCount);
        List<String> tables = db.getTables();
        tables.forEach(table -> executor.submit(() -> this.exportTableSql(dirPath, table)));
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("线程池等待出错");
        }
    }

    private void exportTableSql(String dirPath, String table) {
        System.out.printf("正在导出 %s 表数据, 总记录数: %d \n", table, db.getTableCount(table));
        String fileName = table + ".sql";
        try {
            FileUtil fileUtil = new FileUtil(dirPath, fileName);
            writeTableSql(fileUtil, table);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("创建");
        }
    }

    private void writeTableSql(FileUtil fileUtil, String table) {
        int tableCount = db.getTableCount(table);
//            System.out.printf("table: %s, count: %s \n", table, tableCount);
        String tableHeaderDoc = getTableHeaderDoc(table);
        String createTableSql = db.getCreateTableSql(table);
        String dataHeaderDoc = getDataHeaderDoc(table);
        // 写入表头部
        fileUtil.append(tableHeaderDoc);
        fileUtil.append(createTableSql);
        fileUtil.append(systemSeparator);
        fileUtil.append(dataHeaderDoc);
        // 写入insert data
        int start = 0;
        while (start < tableCount) {
            String insertIntoSql = db.getDataForInsertSqlString(table, start, saveCount);
            fileUtil.append(insertIntoSql);
            start += saveCount;
        }
    }

    private String getDbHeaderDoc() {
        String baseHeader = "/*\n" +
                "Source Server Version : %s" + systemSeparator +
                "Source Host           : %s" + systemSeparator +
                "Source Database       : %s" + systemSeparator +
                systemSeparator +
                "Date: %s" + systemSeparator +
                "*/" + systemSeparator;
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String res =  String.format(baseHeader, db.getVersion(), db.getHost(), db.getDatabase(), date);
        res += systemSeparator + "SET FOREIGN_KEY_CHECKS=0;";
        return res;
    }

    private String getTableHeaderDoc(String table) {
        String base = systemSeparator +
                "-- ----------------------------" + systemSeparator +
                "-- Table structure for %s" + systemSeparator +
                "-- ----------------------------" + systemSeparator +
                "DROP TABLE IF EXISTS `%s`;" + systemSeparator;

        return String.format(base, table, table);
    }

    private String getDataHeaderDoc(String table) {
        String base = systemSeparator +
                "-- ----------------------------" + systemSeparator +
                "-- Records of %s" + systemSeparator +
                "-- ----------------------------" + systemSeparator;
        return String.format(base, table);
    }

    public static void main(String[] args) {
        MyDbExport myDbExport = new MyDbExport("db.properties");
        String filePath = "C:\\Users\\czh\\Desktop\\作品\\test.sql";
        long startTime = System.currentTimeMillis();
//        myDbExport.exportOneFile(filePath);
        myDbExport.exportMultiFile("C:\\Users\\czh\\Desktop\\作品\\test");
        long endTime = System.currentTimeMillis();
        long useTime = endTime - startTime;
        System.out.println("useTime: " + useTime);
        // navicat 3.06  665793行  107,714kb
    }
}
