package com.czh.util.database;

import com.czh.util.database.base.Database;

import java.io.IOException;
import java.util.List;

/**
 * @author czh
 * 数据库测试类
 */
public class DbTest {
	private static String exportDirPath = "C:\\Users\\czh\\Desktop\\作品";
    public static void main(String[] args) throws IOException {
        Database db = new Database("db.properties");
        List<String> tables = db.getTables();
        System.out.println(tables.size());
//        tables.forEach(System.out::println);
        // app.info4  currency_code38
//        System.out.println(db.getTableCount("app_info"));
    }
}
