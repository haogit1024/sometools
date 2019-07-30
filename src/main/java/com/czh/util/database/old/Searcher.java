package com.czh.util.database.old;


import com.czh.util.database.entity.Field;
import com.czh.util.util.PropertiesLoader;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库搜索工具数据库操作逻辑类 TODO 移植到新的类和完成读取文件操作
 * @author czh
 */
public class Searcher {

    public static String URL = "jdbc:mysql://localhost:3306/czh?autoReconnect=true&useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
    public static String USERNAME = "root";
    public static String PASSWORD = "root%df";
    private static String DRIVER = "com.mysql.jdbc.Driver";
    private static String HOST = "";
    private static String PORT = "";
    private static Connection CONN;
    private static PreparedStatement pst;
    private static ResultSet resultSet;
    public static String DATABASE = "yct_server_bak";

    // 查询字段信息sql
    // select * from information_schema.COLUMNS where table_name = 'pay_order' and table_schema = 'cy_pay';

    {
        try {
            PropertiesLoader propertiesLoader = new PropertiesLoader("db.properties");
            USERNAME = propertiesLoader.getProperty("username");
            PASSWORD = propertiesLoader.getProperty("password");
            DRIVER = propertiesLoader.getProperty("driver");
            HOST = propertiesLoader.getProperty("host");
            PORT = propertiesLoader.getProperty("port");
            DATABASE = propertiesLoader.getProperty("database");
//            URL = "jdbc:mysql://"+ HOST +":"+ PORT +"/"+ DATABASE +"?autoReconnect=true&useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
            System.out.println(URL);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("加载配置文件出错");
        }

        try {
            // 检查jvm加载的类，Class.forName返回一个类
            Class.forName(DRIVER);
            CONN = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("链接数据库出错，请检查数据库相关信息");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("未找到mysql驱动");
        }
    }

    /**
     * 获取所有表名
     * @return
     * @throws SQLException
     */
    public List<String> getTableNames() throws SQLException{
        String sql = "show tables";
        pst = CONN.prepareStatement(sql);
        resultSet = pst.executeQuery();
        List<String> tableNames = new ArrayList<>();
        while (resultSet.next()) {
            tableNames.add(resultSet.getString(1));
        }
        return tableNames;
    }

    public List<String> getFieldsByTableName(String tableName, String database) throws SQLException {
        String sql = "select COLUMN_NAME from information_schema.COLUMNS where table_name = '" + tableName
                + "' and table_schema = '" + database + "'";
        PreparedStatement ps = CONN.prepareStatement(sql);
        ResultSet resultSet = ps.executeQuery();
        List<String> fields = new ArrayList<>();
        while (resultSet.next()) {
            String fieldName = resultSet.getString(1);
            fields.add(fieldName);
        }
        return fields;
    }

    /**
     * 查询表所有字段信息 TODO 移植到新的类
     * @param tableName 表名
     * @param database  数据库名
     * @return list
     * @throws SQLException
     */
    public List<Field> getFieldForTable(String tableName, String database) throws SQLException {
        String sql = "select * from information_schema.COLUMNS where table_name = '" + tableName
                + "' and table_schema = '" + database + "'";
        PreparedStatement ps = CONN.prepareStatement(sql);
        ResultSet resultSet = ps.executeQuery();
        List<Field> fields = new ArrayList<>();
        while (resultSet.next()) {
            // TODO 解析结果集，生产Field

        }
        return fields;
    }

    /**
     * 查字段
     * 根据内容查找表中的字段
     * @param tableName 数据库表明
     * @param content   要查找的内容
     * @throws SQLException
     */
    public void searchFieldForTableName(String tableName, String content) throws SQLException {
        String sql = "select COLUMN_NAME from information_schema.COLUMNS where table_name = '" + tableName + "'";
        PreparedStatement ps = CONN.prepareStatement(sql);
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()) {
            Object fieldName = resultSet.getObject(1);
            if (fieldName.toString().contains(content)) {
                System.out.printf("%s : %s%n", tableName, fieldName.toString());
            }
        }
    }

    public void searchContentByTableName(String tableName, String firstField, String field, String content) {
        String sql = "select count(*) from " + tableName + " where `" + field + "` like '%" + content + "%'";
        try {
            PreparedStatement ps = CONN.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                int length = resultSet.getInt(1);
                int limit = 1000;
                if (length < limit) {
                    limit = length;
                }
                if (length > 0) {
                    this.searchContentByTableName(tableName, firstField, field, content, 0, limit, length);
                }
            } else {
                System.out.printf("数据库为 %s 字段为 %s 搜索内容为 %s 没有记录", tableName, field, content);
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("sql = " + sql);
            System.out.println("message = " + e.getMessage());
//            System.out.println("table name = " + tableName);
//            System.out.println("firstField = " + firstField);
//            System.out.println("field = " + field);
//            System.out.println("content = " + content);
            System.out.println("--------------");
        }
    }

    private void searchContentByTableName(String tableName, String firstField, String field,String content, int offset, int limit, int length) throws SQLException {
        String sql = "select " + firstField + "," +field + " from " + tableName + " where " + field + " like '%" + content +
                "%'" + " limit " + offset + " , " + limit;
        PreparedStatement ps = CONN.prepareStatement(sql);
        ResultSet resultSet = ps.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        while (resultSet.next()) {
            Object targetVal = resultSet.getObject(field);
            Object firstVal = resultSet.getObject(firstField);
            System.out.printf("%s  %s:%s  %s:%s", tableName, firstField, firstVal, field, targetVal);
            System.out.println();
        }
    }

}
