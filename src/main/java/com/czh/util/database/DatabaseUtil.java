package com.czh.util.database;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.czh.util.util.PropertiesLoader;

public class DatabaseUtil {
	/*========链接数据库参数========*/
	private String host;
	private String port;
	private String username;
	private String password;
	private String database;
	private String driver;
	
	/*========数据库connection========*/
	private Connection connection;
	
	/*========数据库中所有的表========*/
	private List<String> tables;

	public DatabaseUtil(String propertiesFilePath) {
		if (StringUtils.isBlank(propertiesFilePath)) {
			System.err.println("读取数据库配置信息文件不能为空");
			System.exit(0);
		}
		try {
			PropertiesLoader loader = new PropertiesLoader(propertiesFilePath);
			this.host = loader.getProperty("host");
			this.port = loader.getProperty("port");
			this.username = loader.getProperty("username");
			this.password = loader.getProperty("password");
			this.database = loader.getProperty("database");
			this.driver = loader.getProperty("driver");
			this.connent();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("读取配置文件出错");
			System.exit(0);
		}
	}
	
	public DatabaseUtil(String host, String port, String username, String password, String database, String driver) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.database = database;
		this.driver = driver;
		this.connent();
	}
	
	private void connent() {
        String url = "jdbc:mysql://"+ this.host +":"+ this.port +"/"+ this.database 
        		+"?autoReconnect=true&useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false&serverTimezone=UTC";
        try {
            Class.forName(driver);
            this.connection = DriverManager.getConnection(url, username, password);
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
	
	private ResultSet executeSql(String sql) {
		try {
			PreparedStatement pst =  this.connection.prepareStatement(sql);
            return pst.executeQuery();
		} catch (SQLException e) {
			return null;
		}
	}
	
	private List<String> reduceList(ResultSet resultSet) {
		List<String> res = new ArrayList<String>();
		try {
			if (resultSet == null) {
				return res;
			}
			while (resultSet.next()) {
				res.add(resultSet.getString(1));
			}	
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("reduceList出错");
		}
		return res;
	}
	
	private List<Map<String, String>> reduceMaps(ResultSet resultSet){
		List<Map<String, String>> res = new ArrayList<Map<String,String>>();
		try {
			if (resultSet == null) {
				return res;
			}
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int columnCount = resultSetMetaData.getColumnCount();
			while (resultSet.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 1; i <= columnCount; i++) {
					map.put(resultSetMetaData.getColumnClassName(i), resultSet.getString(i));
				}
				res.add(map);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("reduceMaps出错");
		}
		return res;
	}
	
	public List<String> getTables() {
		if (tables == null || tables.size() == 0) {
			String sql = "show tables";
			ResultSet resultSet = this.executeSql(sql);
			tables = this.reduceList(resultSet);
		}
		return tables;	
	}
	
	public List<String> searchFieldContent(String content) {
		// TODO 遍历表获取表的字段，用java字符串对比
		return null;
	}
	
	public List<String> searchValueContent(String content) {
		return null;
	}
	
	private List<String> simpleSearchValueContent(String content) {
		return null;
	}
	
	private List<String> multiSearchValueContent(String content) {
		return null;
	}
	
	private void searchFieldValueTask(String table, String field, String content, List<String> res) {
		
	}

	public static void main(String[] args) {
		DatabaseUtil databaseUtil = new DatabaseUtil("db.properties");
		List<String> tables = databaseUtil.getTables();
		tables.forEach(System.out::println);
	}

}
