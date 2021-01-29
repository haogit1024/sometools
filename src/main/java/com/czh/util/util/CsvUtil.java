package com.czh.util.util;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenzh
 * @date 2021/1/25
 */
public class CsvUtil {
    private String fileName;
    private String title;
    private String[] titleArray;
    private Object[] body;

    private final String comma = ",";


    private CsvUtil(String... titles) {
        this.titleArray = titles;
    }

    private CsvUtil(String fileName) {
        this.fileName = fileName;
    }

    public static CsvUtil fileName(String fileName) {
        return new CsvUtil(fileName);
    }

    public CsvUtil title(String title) {
        this.title = title;
        return this;
    }
    public CsvUtil title(String... titles) {
        this.titleArray = titles;
        return this;
    }

    public CsvUtil body(Object... body) {
        this.body = body;
        return this;
    }

    public void writer() {
        // 生成标题
        String title = this.title;
        if (titleArray != null && titleArray.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : titleArray) {
                sb.append(s).append(comma);
            }
            title = sb.toString();
        }
        // 创建保存文件夹
        File csvFile = new File(this.fileName);
        File parentFile = csvFile.getParentFile();
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                throw new RuntimeException("创建保存文件夹失败");
            }
        }
        // 保存文件
        try(BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(this.fileName), System.getProperty("sun.jnu.encoding"))
//                        new FileOutputStream(this.fileName), StandardCharsets.UTF_8)
        )) {
            if (title != null && !title.equals("")) {
                writer.write(title);
            }
            Map<Class<?>, Field[]> fieldCache = new HashMap<>();
            if (body != null && body.length > 0) {
                for (Object o : body) {
                    Class<?> clazz = o.getClass();
                    Field[] fields = fieldCache.computeIfAbsent(clazz, k -> clazz.getDeclaredFields());
                    System.out.println("fields length: " + fields.length);
                    for (Field field : fields) {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        Object value = field.get(o);
                        if (value != null) {
                            writer.write(value.toString() + comma);
                        }
                    }
                    writer.write("\n");
                }
            }
        } catch (IOException | IllegalAccessException e ) {
            e.printStackTrace();
            throw new RuntimeException("写入文件出错");
        }
    }
}
