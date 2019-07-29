package com.czh.util.database.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 保存表信息
 * @author czh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Table {
    private String name;
    private String engine;
    private String autoIncrement;
    private String charset;
    private String comment;

    private List<Field> fieldList;
}
