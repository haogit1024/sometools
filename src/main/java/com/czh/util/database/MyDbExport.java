package com.czh.util.database;

import com.czh.util.database.base.Database;
import com.czh.util.util.FileUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    public MyDbExport(String url, String username, String password, String driver) {
        db = new Database(url, username, password, driver);
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
            int tableCount = db.getTableCount(table);
            System.out.printf("table: %s, count: %s \n", table, tableCount);
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
        fileUtil.close();
    }

    public void exportMultiFile(String dirPath, String fileName) {
        String filePath = dirPath + java.io.File.separator + fileName;
        exportMultiFile(filePath);
    }

    public void exportMultiFile(String filePath) {

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
        String filePath = "C:\\Users\\admin\\Desktop\\logs\\test.sql";
        long startTime = System.currentTimeMillis();
        myDbExport.exportOneFile(filePath);
        long endTime = System.currentTimeMillis();
        long useTime = endTime - startTime;
        System.out.println("useTime: " + useTime);
        // navicat 3.06  665793行  107,714kb
    }
}
