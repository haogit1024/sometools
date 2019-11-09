package com.czh.util.database;

import com.czh.util.database.base.Database;

import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * @author czh
 * 数据导出类
 */
public class DbExport {
    public static void main(String[] args) {
        String exportDirPath = "C:\\Users\\czh\\Desktop\\作品";
        File exportDir = new File(exportDirPath);
        // 创建缓存文件夹
        if (!exportDir.exists()) {
            boolean b = exportDir.mkdir();
            if (!b) {
                System.out.println("创建导出文件夹失败");
                System.exit(0);
            }
        }
        Database db = new Database("db.properties");
        Scanner in = new Scanner(System.in);
        List<String> tables = db.getTables();
        int allCount = db.getAllCount();
        System.out.println("表个数: " + tables.size());
        System.out.println("总记录数: " + allCount);
        System.out.println("是否导出多个文件 0.否 1.是");
        System.out.println("导出多个文件速度更快, 但更耗资源（内存，数据库负载）");
        String isMultipartFile = in.nextLine();
        if ("1".equals(isMultipartFile)) {
            saveMultiFile(db, tables, allCount);
        } else {
            saveOneFile(db, tables, allCount);
        }
    }

    public static void saveMultiFile(Database db, List<String> tables, int allCount) {
        // TODO 实现
    }

    public static void saveOneFile(Database db, List<String> tables, int allCount) {
        // TODO 实现
    }
}
