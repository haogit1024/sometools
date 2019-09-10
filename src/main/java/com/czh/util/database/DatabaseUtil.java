package com.czh.util.database;

import com.czh.util.util.PropertiesLoader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author czh
 */
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

	/*=========数据库中所有的表========*/
	private List<String> tables;

	/*=====性能模式：是否开启多线程=====*/
	private boolean powerMode = false;
	private final int cpuCount = Runtime.getRuntime().availableProcessors();
	private final int threadCount = cpuCount;
	private final List<Connection> connectionList = new ArrayList<Connection>(threadCount);
	private int connListIndex = 0;

	/**
	 * 根据一个properties文件初始化，可以是resource的相对路径和绝对路径
	 * @param propertiesFilePath
	 */
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
			this.connection = this.connect();
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
		this.connection = this.connect();
	}

	/**
	 * 返回一个sql connection
	 */
	private Connection connect() {
        String url = "jdbc:mysql://"+ this.host +":"+ this.port +"/"+ this.database
        		+"?autoReconnect=true&useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false&serverTimezone=UTC";
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

	public boolean isPowerMode() {
		return powerMode;
	}

	/**
	 * 设置是否开启性能模式。性能模式下创建数据库连接池，取消性能模式关闭连接池
	 * @param powerMode
	 */
	public void setPowerMode(boolean powerMode) {
		if (powerMode) {
			for (int i = 0; i < threadCount; i++) {
				this.connectionList.add(this.connect());
			}
		} else {
			this.connectionList.forEach((conn) -> {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
		}
		this.powerMode = powerMode;
	}
	
	private Connection getConnection() {
		Connection res; 
		if (isPowerMode()) {
			res = this.connectionList.get(connListIndex);
			this.connListIndex++;
			if (this.connListIndex == this.connectionList.size()) {
				this.connListIndex = 0;
			}
		} else {
			res = this.connection;
		}
		return res;
	}
	
	/**
	 * 执行一条sql，并返回一个ResultSet结果集
	 * @param sql sql command
	 * @return 结果集
	 */
	private ResultSet executeSql(String sql) {
		try {
			PreparedStatement pst =  this.connection.prepareStatement(sql);
            return pst.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("运行sql出错，sql: " + sql);
			return null;
		}
	}
	
	/**
	 * 执行一条sql，并返回一个ResultSet结果集。符合开闭原则，新添加性能模式，不影响原来的使用
	 * @param sql sql command
	 * @return
	 */
	private ResultSet powerExecuteSql(String sql) {
		try {
			PreparedStatement pst =  this.getConnection().prepareStatement(sql);
            return pst.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("运行sql出错，sql: " + sql);
			return null;
		}
	}

	/**
	 * 解析结果集为一个List, 只读取结果集每一行的第一个元素
	 * @param resultSet 执行sql返回的结果集
	 * @return List
	 */
	private List<String> reduceFirstList(ResultSet resultSet) {
		List<String> res = new ArrayList<String>();
		try {
			if (resultSet == null) {
				return res;
			}
			while (resultSet.next()) {
				res.add(resultSet.getString(1));
			}
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("reduceList出错");
		}
		return res;
	}

	/**
	 * 将结果集解析为一个List<List> 结果集的每一行映射成一个List
	 * @param resultSet sql执行后的结果集
	 * @return List<List>
	 */
	private List<List<String>> reduceList(ResultSet resultSet) {
		List<List<String>> res = new ArrayList<>();
		try {
			if (resultSet == null) {
				return res;
			}
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int columnCount = resultSetMetaData.getColumnCount();
			while (resultSet.next()) {
				List<String> list = new ArrayList<>(columnCount);
				for (int i = 1; i <= columnCount ; i++) {
					list.add(resultSet.getString(i));
				}
				res.add(list);
			}
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("reduceList出错");
		}
		return res;
	}

	/**
	 * 将结果集解析为一个List<Map> 结果集的每一行映射成一个map
	 * @param resultSet sql执行后的结果集
	 * @return List<Map>
	 */
	private List<Map<String, String>> reduceMaps(ResultSet resultSet){
		List<Map<String, String>> res = new ArrayList<Map<String,String>>();
		try {
			if (resultSet == null) {
				return res;
			}
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int columnCount = resultSetMetaData.getColumnCount();
			while (resultSet.next()) {
				Map<String, String> map = new HashMap<>(columnCount);
				for (int i = 1; i <= columnCount; i++) {
					map.put(resultSetMetaData.getColumnClassName(i), resultSet.getString(i));
				}
				res.add(map);
			}
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("reduceMaps出错");
		}
		return res;
	}

	/**
	 * 获取数据库中的所有表名称
	 * @return List
	 */
	public List<String> getTables() {
		if (tables == null || tables.size() == 0) {
			String sql = "show tables";
			ResultSet resultSet = this.executeSql(sql);
			tables = this.reduceFirstList(resultSet);
		}
		return tables;
	}

	/**
	 * 获取一个表中的所有字段名
	 * @param table 表名
	 * @return List
	 */
	private List<String> listFields(String table) {
		String sql = "select COLUMN_NAME from information_schema.COLUMNS where table_name = '%s' and table_schema = '%s'";
		sql = String.format(sql, table, this.database);
		ResultSet resultSet = this.executeSql(sql);
		return this.reduceFirstList(resultSet);
	}

	/**
	 * 查询包含content的字段
	 * @param content 需要查找的内容
	 * @return List
	 */
	public List<String> searchFieldContent(String content) {
		List<String> res = new ArrayList<>();
		List<String> tables = this.getTables();
		tables.forEach((table) -> {
			List<String> fields = this.listFields(table);
			fields.forEach((field) -> {
				if (field.contains(content)) {
					res.add(String.format("table: %s, field: %s", table, field));
				}
			});
		});
		return res;
	}

	/**
	 * 查询包含content的值
	 * @param content 需要查找的内容
	 * @return List
	 */
	public List<String> searchValueContent(String content) {
		if (isPowerMode()) {
			return this.multiSearchValueContent(content);
		} else {
			return this.simpleSearchValueContent(content);
		}
	}

	/**
	 * 单线程查询包含content的值
	 * @param content 需要查找的内容
	 * @return List
	 */
	private List<String> simpleSearchValueContent(String content) {
		List<String> res = new ArrayList<>();
		List<String> tables = this.getTables();
		tables.forEach((table) -> {
			List<String> fields = this.listFields(table);
			fields.forEach((field) -> this.searchFieldValueTask(table, fields.get(0), field, content, res));
		});
		return res;
	}

	/**
	 * 多线程查询包含content的值
	 * @param content 需要查找的内容
	 * @return List
	 */
	private List<String> multiSearchValueContent(String content) {
		List<String> tables = this.getTables();

		return null;
	}

	/**
	 * 查询值任务。划分粒度：一个查询任务，查询一个表中的一个字段是否包含content（一个线程只查询一个表中的一个字段）
	 * @param table    表名
	 * @param field    字段名
	 * @param content  需要查找的内容
	 * @param res      需要手机查询结构的List
	 */
	private void searchFieldValueTask(String table, String firstField, String field, String content, List<String> res) {
		String sql = "select `%s`, `%s` from `%s` where `%s` like ";
		sql = String.format(sql, firstField, field, table, field);
		sql += " '%" + content + "%'";
		ResultSet resultSet = this.powerExecuteSql(sql);
		List<List<String>> resLists = this.reduceList(resultSet);
		resLists.forEach((list) -> {
			//"table: %s, firstField: %s, firstValue: %s. field: %s, value: %s"
			res.add(String.format("table: %s, firstField: %s, firstValue: %s. field: %s, value: %s", table, firstField
					, list.get(0), field, list.get(1)));
		});
	}

	public static void main(String[] args) {
//		DatabaseUtil databaseUtil = new DatabaseUtil("db.properties");
//		List<String> res = databaseUtil.searchValueContent("18813365177");
//		List<String> res = databaseUtil.searchFieldContent("id");
//		res.forEach(System.out::println);
		System.out.println(Runtime.getRuntime().availableProcessors());
	}

}
