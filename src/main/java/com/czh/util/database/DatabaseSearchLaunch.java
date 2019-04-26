package com.czh.util.database;


import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class DatabaseSearchLaunch {
    private static int MAX_THREAD_NUM = 16;
    public static void main(String[] args) throws SQLException {
        Scanner in = new Scanner(System.in);
        setConnPar(in);
        selectSearcherType(in);
    }

    private static void setConnPar(Scanner in) {
        System.out.println("1 简单模式(代码数据库配置信息)  2 复杂模式(输入数据库配置信息)");
        String typeCommand = in.nextLine();
        if (typeCommand.equals("1")) {
//            System.out.println("请输入数据库名");
//            String databaseName = in.nextLine();
//            Searcher.URL = "jdbc:mysql://localhost:3306/" + databaseName + "?useSSL=false";
        } else if (typeCommand.equals("2")) {
            System.out.println("复杂模式, 暂未完成");
            System.out.println("请输入ip地址");
            String ip = in.nextLine();
            System.out.println("请输入端口号");
            String port = in.nextLine();
            System.out.println("请输入数据库名");
            String databaseName = in.nextLine();
            System.out.println("请输入用户名");
            String user = in.nextLine();
            System.out.println("请输入密码");
            String password = in.nextLine();

            Searcher.URL = "jdbc:mysql://" + ip + ":" + port + "/" + databaseName + "?useSSL=false";
            Searcher.username = user;
            Searcher.password = password;
        } else if (typeCommand.equals("-1")) {
            System.out.println("退出系统");
        } else {
            System.out.println("未知命令, 请重新输入");
            //递归执行
            setConnPar(in);
        }
    }

    private static void selectSearcherType(Scanner in) throws SQLException {
        Searcher searcher = new Searcher();
        List<String> tableNames = searcher.getTableNames();
        System.out.println("找到了表数量为: " + tableNames.size());
        System.out.println("1 搜索字段名字  2 搜索内容");
        String typeCommand = in.nextLine();

        String content;

        switch (typeCommand) {
            case "1":
                System.out.println("请输入搜索内容");
                content = in.nextLine();
                for (String name : tableNames) {
                    searcher.searchFieldForTableName(name, content);
                }
                break;
            case "2":
                System.out.println("请输入搜索内容");
                content = in.nextLine();
                simpleSearchContent(searcher, content);
                break;
            case "-1":
                break;
            default:
                System.out.println("未知命令,请重新输入");
                selectSearcherType(in);
        }

    }

    private static void simpleSearchContent(Searcher searcher, String content) throws SQLException {
        List<String> tableNames = searcher.getTableNames();
        int i = 1;
        int tableIndex = 0;
        for (String tableName : tableNames) {
            tableIndex++;
//            System.out.println("------table name = " + tableName + " index = " + tableIndex++);
            List<String> fields = searcher.getFieldsByTableName(tableName, Searcher.database);
            if (fields.size() == 0) {
                System.out.printf("table: %s , field size : %d, index: %d \n", tableName, fields.size(), tableIndex);
                continue;
            }
            String firstField = fields.get(0);
            Semaphore semaphore = new Semaphore(MAX_THREAD_NUM);
            for (String field : fields) {
                new Thread(() -> {
                    try {
                        semaphore.acquire();
                        searcher.searchContentByTableName(tableName, firstField, field, content);
                        semaphore.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                i++;
            }
        }
        System.out.println(i);
//        searcher.searchContentByTableName("ims_pay_orders","trans_id","trans_id","4005362001201703234316783718");
    }
}
