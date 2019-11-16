package com.czh.util.database;

import com.czh.util.database.base.FileUtil;

import java.io.IOException;

/**
 * @author czh
 * 数据库测试类
 */
public class DbTest {
	private static String exportDirPath = "C:\\Users\\czh\\Desktop\\作品";
    public static void main(String[] args) throws IOException {
        FileUtil fileUtil = new FileUtil("C:\\Users\\czh\\Desktop\\作品", "fuck.sql");
        fileUtil.append("hello sql");
        fileUtil.close();
    }
}
