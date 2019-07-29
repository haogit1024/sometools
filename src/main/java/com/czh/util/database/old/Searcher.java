package com.czh.util.database.old;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Searcher {
//    public static String URL = "jdbc:mysql://localhost:3306/czh?useSSL=false";
//public static String username = "root";
//    public static String password = "root";
    // TODO 再配置文件读入设置
    public static String URL = "jdbc:mysql://localhost:3306/czh?useSSL=false";
    public static String username = "root";
    public static String password = "root%df";
    private static String driver = "com.mysql.jdbc.Driver";
    private static Connection conn;
    private static PreparedStatement pst;
    private static ResultSet resultSet;
    public static String database = "yct_server_bak";


    {
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(URL, username, password);
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
        pst = conn.prepareStatement(sql);
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
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet resultSet = ps.executeQuery();
        List<String> fields = new ArrayList<>();
        while (resultSet.next()) {
            String fieldName = resultSet.getString(1);
            fields.add(fieldName);
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
        PreparedStatement ps = conn.prepareStatement(sql);
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
            PreparedStatement ps = conn.prepareStatement(sql);
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
        PreparedStatement ps = conn.prepareStatement(sql);
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
