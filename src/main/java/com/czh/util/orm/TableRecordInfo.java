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

    private String getConditionFromFieldValueMap() {
        StringBuilder sb = new StringBuilder();
        fieldValueMap.forEach((field, value) -> {
            String baseCondition;
            if (Utils.isNumber(value)) {
                baseCondition = "and `%s`=%s ";
            } else {
                baseCondition = "and `%s`=\"%s\" ";
            }
            sb.append(String.format(baseCondition, Utils.toLine(field), value.toString()));
        });
        return sb.toString();
    }

    private String getConditionFromFieldValueMap(String... conditionFields) {
        StringBuilder sb = new StringBuilder();
        for (String conditionField : conditionFields) {
            Object value = fieldValueMap.get(conditionField);
            if (value == null) {
                continue;
            }
            String baseCondition;
            if (Utils.isNumber(value)) {
                baseCondition = "and `%s`=%s ";
            } else {
                baseCondition = "and `%s`=\"%s\" ";
            }
            sb.append(String.format(baseCondition, Utils.toLine(conditionField), value.toString()));
        }
        return sb.toString();
    }

    private String getSetStatementFromFieldValueMap(List<String> fields) {
        StringBuilder setBuilder = new StringBuilder();
        fields.forEach(field -> {
            String baseSet;
            Object value = fieldValueMap.get(field);
            if (Utils.isNumber(value)) {
                baseSet = "`%s`=%s" + this.separator;
            } else {
                baseSet = "`%s`=\"%s\"" + this.separator;
            }
            setBuilder.append(String.format(baseSet, Utils.toLine(field), value.toString()));
        });
        String ret = setBuilder.toString();
        if (!"".equals(ret)) {
            ret = ret.substring(0, ret.length() - this.separator.length());
        }
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
        return baseSql + this.getConditionFromFieldValueMap();
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
        // 如果 conditionFields 空, 不生成sql
        if (conditionFields.length == 0) {
            return null;
        }
        final String baseSql = "update `" + this.tableName + "` set %s where 1=1 %s";
        List<String> fieldList = getFieldListFromFieldValueMap();
        fieldList.removeAll(Arrays.asList(conditionFields));
        // 1. 生成 setStatement 2. 生成whereStatement
        String setStatement = getSetStatementFromFieldValueMap(fieldList);
        System.out.println("setStatement : " + setStatement);
        String conditionStatement = getConditionFromFieldValueMap(conditionFields);
        if (StringUtils.isBlank(setStatement) || StringUtils.isBlank(conditionStatement)) {
            return null;
        }
        return String.format(baseSql, setStatement, conditionStatement);
    }

    public String convertToUpdateByIdSql() {
        String baseSql = "update `" + this.tableName + "` set %s where id = %s";
        Object idVal = this.fieldValueMap.get("id");
        if (idVal == null) {
            return null;
        }
        List<String> fields = getFieldListFromFieldValueMap();
        fields.remove("id");
        String setStatement = getSetStatementFromFieldValueMap(fields);
        return String.format(baseSql, setStatement, idVal.toString());
    }

    public String convertToDeleteSql(String... conditionFields) {
        String baseSql = "delete from `" + this.tableName + "` where 1=1 ";
        String conditionStatement = getConditionFromFieldValueMap(conditionFields);
        if (StringUtils.isBlank(conditionStatement)) {
            return null;
        }
        return baseSql + conditionStatement;
    }

    public String convertToDeleteById() {
        String baseSql = "delete from `" + this.tableName + "` where id=%s";
        Object idVal = fieldValueMap.get("id");
        return String.format(baseSql, idVal);
    }
}
