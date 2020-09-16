package com.czh.util.orm;

import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 1. 初始化 jdbc 连接
 * 2. 反射获取类名为表名, 类属性为字段名, 类属性值为值
 * 3. 将结果集反序列化为对象
 * @author chenzh
 * @date 2020/9/15
 */
public class ORMDataBase {
    private String host;
    private String port;
    private String username;
    private String password;
    private String database;
    private String driver;
    private String url;
    private Connection connection;

    public ORMDataBase(String host, String port, String username, String password, String database, String driver) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.driver = driver;
        this.connection = this.connect();
    }

    public ORMDataBase(String url, String username, String password, String driver) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driver = driver;

        // 解析url获取data, host, port等信息
        int beginIndex = url.indexOf("//") + 2;
        int endIndex = url.indexOf("?");
        if (beginIndex == 1 || endIndex == -1) {
            throw new RuntimeException("创建Database失败, url错误");
        }
        // demo 47.102.137.55:3306/lonely
        String addressInfo = url.substring(beginIndex, endIndex);
        String[] addressArray = addressInfo.split(":");
        this.host = addressArray[0];
        this.port = addressArray[1].split("/")[0];
        this.database = addressArray[1].split("/")[1];
        this.connection = this.connect();
    }

    /**
     * 返回一个sql connection
     */
    private Connection connect() {
        String url;
        if (StringUtils.isNotBlank(this.url)) {
            url = this.url;
        } else {
            url = "jdbc:mysql://"+ this.host +":"+ this.port +"/"+ this.database
                    +"?autoReconnect=true&useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false&serverTimezone=UTC";
        }
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("数据库驱动没有找到");
            System.exit(0);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("连接数据库出错，请检查数据相关参数");
            System.exit(0);
        }
        return null;
    }

    private ResultSet executeQuerySql(String sql) {
        try {
            PreparedStatement pst =  this.connection.prepareStatement(sql);
            return pst.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("运行sql出错，sql: " + sql);
            System.exit(0);
            return null;
        }
    }

    public <T> T selectOne(T t) throws SQLException {
        TableRecordInfo recordInfo = new TableRecordInfo(t);
        String sql = recordInfo.convertToSelectSql();
        ResultSet resultSet = executeQuerySql(sql);
        if (resultSet == null || !resultSet.next()) {
            return null;
        }
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        LinkedHashMap<String, Object> map = new LinkedHashMap<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            map.put(resultSetMetaData.getColumnClassName(i), resultSet.getObject(i));
        }
        recordInfo.setFieldValueMap(map);
        return recordInfo.convertToType((Class<T>)t.getClass());
    }

    public <T> List<T> selectList(T t) throws SQLException {
        TableRecordInfo recordInfo = new TableRecordInfo(t);
        String sql = recordInfo.convertToSelectSql();
        ResultSet resultSet = executeQuerySql(sql);
        if (resultSet == null) {
            return null;
        }
        String tableName = recordInfo.getTableName();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        List<TableRecordInfo> tableRecordInfoList = new ArrayList<>();
        while (resultSet.next()) {
            TableRecordInfo info = new TableRecordInfo();
            info.setTableName(tableName);
            LinkedHashMap<String, Object> map = new LinkedHashMap<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                map.put(resultSetMetaData.getColumnClassName(i), resultSet.getObject(i));
            }
            info.setFieldValueMap(map);
            tableRecordInfoList.add(info);
        }
        return tableRecordInfoList.stream()
                .map(tableRecordInfo -> tableRecordInfo.convertToType((Class<T>)t.getClass()))
                .collect(Collectors.toList());
    }
}
