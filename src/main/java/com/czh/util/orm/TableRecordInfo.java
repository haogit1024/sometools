package com.czh.util.orm;

import com.czh.util.util.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 一张表的一条记录信息
 * @author chenzh
 * @date 2020/9/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableRecordInfo {
    private String tableName;
    private ArrayList<String> fieldList;
    private LinkedHashMap<String, Object> fieldValueMap;

    /**
     * 只在 convert 方法中使用
     */
    private final String separator = ", ";
    private final String brackets = "`";

    private ArrayList<String> getFieldListFromFieldValueMap() {
        ArrayList<String> ret = new ArrayList<>(this.fieldValueMap.size());
        this.fieldValueMap.forEach((field, object) -> ret.add(field));
        return ret;
    }

    private ArrayList<Object> getValueListFormFieldValueMap() {
        ArrayList<Object> ret = new ArrayList<>(this.fieldValueMap.size());
        this.fieldValueMap.forEach((field, object) -> ret.add(object));
        return ret;
    }

    /**
     * TableRecordInfo{tableName='abcde', fieldList=[a, b, c], fieldValueMap={k1:v1, k2:v2, k3:v3}}
     */
    @Override
    public String toString() {
        final String baseString = "TableRecordInfo{tableName='%s', fieldList=[%s], fieldValueMap={%s}}";
        String fieldListString = Utils.listToString(fieldList, separator);
        StringBuilder valueBuilder = new StringBuilder();
        fieldValueMap.forEach((field, value) -> valueBuilder.append(field)
                .append(": ").append(value.toString()).append(separator));
        String valueString = valueBuilder.toString();
        if (!"".equals(valueString)) {
            valueString = valueString.substring(0, valueString.length() - separator.length());
        }
        return String.format(baseString, this.tableName, fieldListString, valueString);
    }

    public TableRecordInfo(Object object) {
        fieldList = new ArrayList<>();
        fieldValueMap = new LinkedHashMap<>();
        this.tableName = Utils.classNameToTableName(object.getClass().getName());
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            this.fieldList.add(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                Object value = field.get(object);
                if (value != null) {
                    this.fieldValueMap.put(fieldName, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 转换为对应的类型, 只是简单的映射一下 T 类的字段, 如果对应则映射值
     * @param clazz
     * @param <T>
     * @return
     */
    public <T>T convertToType(Class<T> clazz) {
        T t;
        try {
            t = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Object value = this.fieldValueMap.get(field.getName());
            if (value != null) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    field.set(t, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return t;
    }

    /**
     * 转换成查询语句, fieldValueMap不为空的值为条件
     * 当fieldValueMap中的 a 和 b 不为空时生成以下sql
     * demo: select * from this.tableName where a = 'a' and b = 'c'
     * @return
     */
    public String convertToSelectSql() {
        if (StringUtils.isBlank(this.tableName)) {
            return null;
        }
        String baseSql = "select * from `" + this.tableName + "` ";
        StringBuilder sb = new StringBuilder();
        sb.append(baseSql);
        if (fieldValueMap != null && fieldValueMap.size() > 0) {
            // 避免sql错误
            sb.append("where 1 = 1 ");
            fieldValueMap.forEach((field, value) -> {
                String baseCondition;
                if (value instanceof Byte ||
                    value instanceof Short ||
                    value instanceof Integer ||
                    value instanceof Long) {
                    baseCondition = "and `%s`=%s ";
                } else {
                    baseCondition = "and `%s`=\"%s\" ";
                }
                sb.append(String.format(baseCondition, Utils.toLine(field), value.toString()));
            });
        }
        return sb.toString();
    }

    public String convertToInsertSql() {
        final String baseSql = "insert into `" + this.tableName + "` (%s) values(%s)";
        List<String> fieldList = this.getFieldListFromFieldValueMap();
        List<Object> valueList = this.getValueListFormFieldValueMap();
        String fieldListString = Utils.listToString(fieldList, separator, brackets, true, false);
        String valueListString = Utils.listToString(valueList, separator, "", false, true);
        return String.format(baseSql, fieldListString, valueListString);
    }

    public String convertToUpdateSql(String... conditionFields) {
        final String baseSql = "update `" + this.tableName + "` %s where %s";
        List<String> fieldList = new ArrayList<>(this.fieldValueMap.size());

        return null;
    }

    public String convertToUpdateByIdSql() {
        return null;
    }
}
