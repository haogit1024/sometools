package com.czh.util.database;



/**
 * @author czh
 * 数据库测试类
 */
public class DbTest {
	private static String exportDirPath = "C:\\Users\\czh\\Desktop\\作品";

    public static void main(String[] args) {
        String url = "jdbc:mysql://47.102.137.55:3306/lonely?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true";
        int beginIndex = url.indexOf("//") + 2;
        int endIndex = url.indexOf("?");
        String addressInfo = url.substring(beginIndex, endIndex);
        System.out.println(addressInfo);
        String[] addressArray = addressInfo.split(":");
        String host = addressArray[0];
        String port = addressArray[1].split("/")[0];
        String data = addressArray[1].split("/")[0];
    }
}
