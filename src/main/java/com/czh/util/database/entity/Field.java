package com.czh.util.database.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 保存字段信息
 * @author czh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Field {
    private String name;
    private String type;
    private String length;
    private String defaultValue;
    private String comment;
}
