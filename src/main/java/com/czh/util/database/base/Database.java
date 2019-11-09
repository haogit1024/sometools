package com.czh.util.database.base;

import com.czh.util.util.PropertiesLoader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author czh
 */
public class Database {
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
	/*======数据库中所有表的总条数======*/
	private int allCount = -1;
	/*==========每个表的记录数==========*/
	private Map<String, Integer> tableCount;

	/*=====性能模式：是否开启多线程=====*/
	private boolean powerMode = false;
	private final int cpuCount = Runtime.getRuntime().availableProcessors();
	/*=======性能模式下的线程数量=======*/
	private final int threadCount = cpuCount * 2;
	/*=====性能模式下的数据库连接池=====*/
	List<Connection> connectionList = new ArrayList<>(threadCount);
	/*=======数据库连接池索引=======*/
	private int connListIndex = 0;

	/**
	 * 根据一个properties文件初始化，可以是resource的相对路径和绝对路径
	 * @param propertiesPath
	 */
	public Database(String propertiesPath) {
		if (StringUtils.isBlank(propertiesPath)) {
			System.err.println("读取数据库配置信息文件不能为空");
			System.exit(0);
		}
		try {
			PropertiesLoader loader = new PropertiesLoader(propertiesPath);
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

	public Database(String host, String port, String username, String password, String database, String driver) {
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
			// 开启性能模式，创建数据库连接池
			for (int i = 0; i < threadCount; i++) {
				this.connectionList.add(this.connect());
			}
		} else {
			// 关闭性能模式，关闭连接池
			this.connectionList.forEach((conn) -> {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
			connectionList = new ArrayList<>(threadCount);
		}
		this.powerMode = powerMode;
	}
	
	private synchronized Connection getConnection() {
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
	private ResultSetParser executeSql(String sql) {
		try {
			PreparedStatement pst =  this.connection.prepareStatement(sql);
			ResultSet resultSet = pst.executeQuery();
			return new ResultSetParser(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("运行sql出错，sql: " + sql);
			System.exit(0);
			return null;
		}
	}
	
	/**
	 * 执行一条sql，并返回一个ResultSet结果集。符合开闭原则，新添加性能模式，不影响原来的使用
	 * @param sql sql command
	 * @return
	 */
	private ResultSetParser powerExecuteSql(String sql) {
		try {
			PreparedStatement pst =  this.getConnection().prepareStatement(sql);
            ResultSet resultSet =  pst.executeQuery();
            return  new ResultSetParser(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("运行sql出错，sql: " + sql);
			System.exit(0);
			return null;
		}
	}

	/**
	 * 获取数据库中的所有表名称
	 * @return List
	 */
	public List<String> getTables() {
		if (tables == null || tables.size() == 0) {
			String sql = "show tables";
			tables = this.executeSql(sql).reduceFirstLine();
		}
		return tables;
	}
	
	public int getAllCount() {
		if (allCount == -1) {
			int temp = 0;
			List<String> tables = this.getTables();
			for (String table : tables) {
				temp += this.getTableCount(table);
			}
			allCount = temp;
		}
		return allCount;
	}

	/**
	 * 获取每个表中的记录数
	 * @param table 表名
	 * @return 记录数
	 */
	public int getTableCount(String table) {
		if (tableCount == null) {
			tableCount = new HashMap<>(this.getTables().size());
		}
		Integer count = tableCount.get(table);
		if (count == null) {
			String sql = "select COUNT(*) from `%s`";
			sql = String.format(sql, table);
			String realCount = this.executeSql(sql).reduceFirstLine().get(0);
			count = Integer.parseInt(realCount);
			tableCount.put(table, count);
		}
		return count;
	}
	
	/**
	 * 获取建表语句
	 * @param table 表名
	 * @return 建表sql语句
	 */
	public String getCreateTableSql(String table) {
		String sql = "show table `%s`";
		sql = String.format(sql, table);
		List<String> res = this.executeSql(sql).reduceFirstLine();
		return res.get(1);
	}

	/**
	 * 获取表数据的insert sql语句
	 * TODO 用Java8函数式编程重构
	 * @param table   表名
	 * @param start   开始索引 (从 0 开始)
	 * @param offset  查找条数
	 * @return
	 */
	public List<String> getDataForInsertSqlList(String table, int start, int offset) {
		List<String> res = new ArrayList<>(offset);
		String sql = "select * from `%s` limit %d, %d";
		sql = String.format(sql, table, start, offset);
		List<List<String>> data = this.executeSql(sql).reduceList();
		String baseInsertSql = "INSERT INTO `%s` VALUES(%s)";
		// 遍历每一条行数据
		for (List<String> row : data) {
			String values = this.listToString(row);
			String insertSql = String.format(baseInsertSql, table, values);
			res.add(insertSql);
		}
		return res;
	}

	/**
	 * 获取表数据的insert sql语句
	 * TODO 用Java8函数式编程重构
	 * @param table   表名
	 * @param start   开始索引 (从 0 开始)
	 * @param offset  查找条数
	 * @return
	 */
	public String getDataForInsertSqlString(String table, int start, int offset) {
		StringBuilder res = new StringBuilder();
		String sql = "select * from `%s` limit %d, %d";
		sql = String.format(sql, table, start, offset);
		List<List<String>> data = this.executeSql(sql).reduceList();
		String baseInsertSql = "INSERT INTO `%s` VALUES(%s)";
		// 遍历每一条行数据
		for (List<String> row : data) {
			String values = this.listToString(row);
			String insertSql = String.format(baseInsertSql, table, values);
			res.append(insertSql).append(System.getProperty("line.separator"));
		}
		return res.toString();
	}

	/**
	 * 获取一个表中的所有字段名
	 * @param table 表名
	 * @return List
	 */
	private List<String> listFields(String table) {
		String sql = "select COLUMN_NAME from information_schema.COLUMNS where table_name = '%s' and table_schema = '%s'";
		sql = String.format(sql, table, this.database);
		return this.executeSql(sql).reduceFirstLine();
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
	private List<String> multiSearchValueContent(String content)  {
		List<String> res = new ArrayList<>();
		List<String> tables = this.getTables();
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		tables.forEach((table) -> {
			List<String> fields = this.listFields(table);
			fields.forEach((field) -> {
				executor.submit(() -> {
					this.searchFieldValueTask(table, fields.get(0), field, content, res);
				});
			});
		});
		executor.shutdown();
		try {
			executor.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("线程池执行错误");
			System.exit(0);
		}
		return res;
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
		List<List<String>> resLists = this.powerExecuteSql(sql).reduceList();
		resLists.forEach((list) -> {
			//"table: %s, firstField: %s, firstValue: %s. field: %s, value: %s"
			res.add(String.format("table: %s, firstField: %s, firstValue: %s. field: %s, value: %s", table, firstField
					, list.get(0), field, list.get(1)));
		});
	}

	/**
	 * list 转 逗号分隔字符串
	 * @param list 字符串列表
	 * @return 逗号分隔字符串
	 */
	private String listToString(List<String> list) {
		if (list == null || list.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		String separator = ", ";
		for (String s : list) {
			String baseItem;
			if (s == null) {
				baseItem = "%s" + separator;
			} else {
				baseItem = "\'%s\'" +separator;
			}
			String itemValue = String.format(baseItem, s);
			sb.append(itemValue);
		}
		String res = sb.toString();
		return res.substring(0, res.length() - separator.length());
	}

	/**
	 * 关闭数据库连接
	 */
	public void close() {
		if (this.connection != null) {
			try {
				this.connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		Database database = new Database("db.properties");
//		List<String> res = databaseUtil.getTables();
//		databaseUtil.setPowerMode(true);
//		long start = System.currentTimeMillis();
//		List<String> res = databaseUtil.searchValueContent("czh");
//		System.out.println((System.currentTimeMillis() - start));
//		res.forEach(System.out::println);
//		List<String> res = databaseUtil.searchFieldContent("id");
//		res.forEach(System.out::println);
//		System.out.println(Runtime.getRuntime().availableProcessors());
//		System.out.println(databaseUtil.getAllCount());
//		databaseUtil.getTables().forEach(System.out::println);
		String sql = database.getDataForInsertSqlString("lonely_user", 0, 100);
		System.out.println(sql);
	}

}
