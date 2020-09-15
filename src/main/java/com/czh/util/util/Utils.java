package com.czh.util.util;

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
        return sb.toString();
    }
}
