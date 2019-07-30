package com.czh.util.database;

import com.czh.util.database.entity.Field;
import com.czh.util.database.entity.Table;
import com.czh.util.util.PropertiesLoader;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MySqlOperate {
    private Connection connection;
    private String database;

    public MySqlOperate() throws IOException {
        PropertiesLoader propertiesLoader = new PropertiesLoader("db.properties");
        String username = propertiesLoader.getProperty("username");
        String password = propertiesLoader.getProperty("password");
        String driver = propertiesLoader.getProperty("driver");
        String host = propertiesLoader.getProperty("host");
        String port = propertiesLoader.getProperty("port");
        database = propertiesLoader.getProperty("database");
        String url = "jdbc:mysql://"+ host +":"+ port +"/"+ database +"?autoReconnect=true&useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false&serverTimezone=UTC";
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("数据库驱动没有找到");
            System.exit(0);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("连接数据库出错，请检查数据相关参数");
            System.exit(0);
        }
    }

    public MySqlOperate(String username, String password, String driver, String host, String port, String database) {
        this.database = database;
        String url = "jdbc:mysql://"+ host +":"+ port +"/"+ database +"?autoReconnect=true&useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false&serverTimezone=UTC";
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("数据库驱动没有找到");
            System.exit(0);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("连接数据库出错，请检查数据相关参数");
            System.exit(0);
        }
    }

    /**
     * 获取所有表名
     * @return
     * @throws SQLException
     */
    public List<String> getTableNames() {
        try {
            String sql = "show tables";
            PreparedStatement pst =  connection.prepareStatement(sql);
            ResultSet resultSet = pst.executeQuery();
            List<String> tableNames = new ArrayList<>();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString(1));
            }
            return tableNames;
        } catch (SQLException e) {
            System.err.println("getTableNames sql error");
            return new ArrayList<>(0);
        }
    }

    /**
     * 获取单个表结构
     * @param tableName
     * @return
     */
    public Table getTable(String tableName) {
        try {
            Table table = new Table();
            String sql = "select * from information_schema.TABLES where table_name = '" + tableName
                    + "' and table_schema = '" + database + "'";
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();
            // TODO 解析结果集

            table.setFieldList(this.getFields(tableName));
            return table;
        } catch (SQLException e) {
            System.err.println("getTableByName sql error");
            return null;
        }
    }

    /**
     * 获取所有表的字段
     * @param tableName
     * @return
     * @throws SQLException
     */
    private List<Field> getFields(String tableName) throws SQLException {
        List<Field> fields = new ArrayList<>();
        String sql = "select * from information_schema.COLUMNS where table_name = '" + tableName
                + "' and table_schema = '" + database + "'";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet resultSet = ps.executeQuery();
        // TODO 解析结果集
        return fields;
    }

    /**
     * 获取所有表结构
     * @return
     */
    public List<Table> getAllTable() {
        List<String> tableNames = this.getTableNames();
        List<Table> tables = new ArrayList<>(tableNames.size());
        for (String tableName: tableNames) {
            tables.add(this.getTable(tableName));
        }
        return tables;
    }

    public List<Map<String, Object>> getData(String tableName, int off, int let) {
        return null;
    }
}
