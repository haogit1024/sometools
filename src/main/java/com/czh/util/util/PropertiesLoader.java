package com.czh.util.util;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

public class PropertiesLoader {
    private Properties properties;
    public PropertiesLoader(@NotNull String fileName) throws IOException {
        assert (fileName != null && !"".equals(fileName)) : "读取的配置文件名不能为空";
        InputStream inputStream = PropertiesLoader.class.getResourceAsStream("/" + fileName);
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
        PropertiesLoader propertiesLoader = new PropertiesLoader("db.properties");
        propertiesLoader.display();
    }
}
