package com.czh.util.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author czh
 */
public class PropertiesLoader {
    private final Properties properties;

    /**
     * 根据一个路径初始化，可以是resource的相对路径和绝对路径
     * @param filePath
     * @throws IOException
     */
    public PropertiesLoader(String filePath) throws IOException {
        assert (StringUtils.isNotBlank(filePath)) : "读取的配置文件路径不能为空";
        InputStream inputStream;
        if (filePath.contains(File.separator)) {
            // 绝对路径
            inputStream = new FileInputStream(filePath);
        } else {
            // resource下的相对路径
            inputStream = PropertiesLoader.class.getResourceAsStream("/" + filePath);;
        }
        properties = new Properties();
        properties.load(inputStream);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public void display() {
    	Set<Map.Entry<Object, Object>> set = properties.entrySet();
    	set.forEach(entry -> System.out.printf("key: %s, val: %s \n", entry.getKey(), entry.getValue()));
    }

    public static void main(String[] args) throws IOException {
        PropertiesLoader propertiesLoader = new PropertiesLoader("D:\\czhcode\\github\\java\\simple\\new_util\\src\\main\\resources\\db.properties");
        propertiesLoader.display();
    }
}
