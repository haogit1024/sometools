package com.czh.util.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.czh.util.database.base.Database;
import com.czh.util.database.base.FileUtil;

/**
 * @author czh
 * 数据库测试类
 */
public class DbTest {
	private static String exportDirPath = "C:\\Users\\czh\\Desktop\\作品";
    public static void main(String[] args) throws IOException {
        Database db = new Database("db.properties");
        /*String sql = db.getCreateTableSql("lonely_user");
        System.out.println(sql);
        String insertSql = db.getDataForInsertSqlString("lonely_user", 0, 100);
        System.out.println(insertSql);*/
        System.out.println(db.getVersion());
    }
}
