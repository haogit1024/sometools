package com.czh.util.orm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author chenzh
 * @date 2020/9/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FileSize {
    private String fileSystem;
    private String parentDir;
    private String filePath;
    private String fileName;
    private Integer fileSize;
    private Integer isDir;
    private Integer scanTime;
}
