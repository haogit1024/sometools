package com.czh.util.database;

import com.czh.util.database.base.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author czh
 * 数据库搜索类
 */
public class DbSearch {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("请输入操作类型 1.搜索表字段名 2.搜索表数据");
        String type = in.nextLine();
        System.out.println("请输入要搜索的内容");
        String searchContent = in.nextLine();
        Database db = new Database("db.properties");
        List<String> res = new ArrayList<>(0);
        if ("1".equals(type)) {
            res = db.searchFieldContent(searchContent);
        } else if ("2".equals(type)) {
            System.out.println("是否要开启多线程模式 0.否 1.是");
            String powerModel = in.nextLine();
            if ("1".equals(powerModel)) {
                db.setPowerMode(true);
            }
            res = db.searchValueContent(searchContent);
        } else {
            System.out.println("未知操作类型");
            System.exit(0);
        }
        res.forEach(System.out::println);
    }
}
