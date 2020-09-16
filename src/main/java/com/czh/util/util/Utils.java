package com.czh.util.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chenzh
 * @date 2020/9/15
 */
public class Utils {
    /**
     * 驼峰 转下划线
     * @param camelCase
     * @return
     */
    public static String toLine(String camelCase){
        Pattern humpPattern = Pattern.compile("[A-Z]");
        Matcher matcher = humpPattern.matcher(camelCase);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()){
            matcher.appendReplacement(sb, "_"+matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        String ret = sb.toString();
        if (ret.charAt(0) == '_') {
            return ret.substring(1);
        }
        return ret;
    }

    public static String classNameToTableName(String className) {
        String[] classNames = className.split("\\.");
        String tableName = classNames[classNames.length - 1];
        return Utils.toLine(tableName);
    }

    public static <T> String listToString(List<T> list, String separator) {
        return listToString(list, separator, "");
    }

    public static<T> String listToString(List<T> list, String separator, String itemBrackets) {
        return listToString(list, separator, itemBrackets, false, false);
    }

    /**
     * list 转 string
     * @param list          列表
     * @param separator     分隔符
     * @param itemBrackets  列表元素括号
     * @param isToLine      是否转成下划线命名法
     * @param isSql         是否需要判断非数值类型自动添加 " , 就和 sql 一样
     * @param <T>           任意实现了 {@link Object#toString()} 的类
     * @return              string
     */
    public static<T> String listToString(List<T> list, String separator, String itemBrackets, boolean isToLine, boolean isSql) {
        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        list.forEach(item -> {
            String itemString = item.toString();
            if (isToLine) {
                itemString = Utils.toLine(itemString);
            }
            if (isSql) {
                // 不是数值类型
                if (!(item instanceof Byte ||
                    item instanceof Short ||
                    item instanceof Integer||
                    item instanceof Long)) {
                    itemString = "\"" + itemString + "\"";
                }
            }
            sb.append(itemBrackets).append(itemString).append(itemBrackets).append(separator);
        });
        String ret = sb.toString();
        if (!"".equals(ret)) {
            ret = ret.substring(0, ret.length() - separator.length());
        }
        return ret;
    }
}
