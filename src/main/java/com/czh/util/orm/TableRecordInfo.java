package com.czh.util.orm;

import java.util.List;
import java.util.Map;

/**
 * 一张表的一条记录信息
 * @author chenzh
 * @date 2020/9/15
 */
public class TableRecordInfo {
    private String tableName;
    private List<String> fieldList;
    private Map<String, Object> fieldValueMap;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<String> fieldList) {
        this.fieldList = fieldList;
    }

    public Map<String, Object> getFieldValueMap() {
        return fieldValueMap;
    }

    public void setFieldValueMap(Map<String, Object> fieldValueMap) {
        this.fieldValueMap = fieldValueMap;
    }

    @Override
    public String toString() {
        return "TableRecordInfo{" +
                "tableName='" + tableName + '\'' +
                ", fieldList=" + fieldList +
                ", fileValueMap=" + fieldValueMap +
                '}';
    }

    /**
     * 转换为对应的类型, 只是简单的映射一下 T 类的字段, 如果对应则映射值
     * @param clazz
     * @param <T>
     * @return
     */
    public <T>T convertToType(Class<T> clazz) {
        return null;
    }
}
