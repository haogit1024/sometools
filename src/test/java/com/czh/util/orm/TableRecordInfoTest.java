package com.czh.util.orm;

import com.czh.util.orm.entity.FileSize;
import junit.framework.TestCase;

public class TableRecordInfoTest extends TestCase {

    public void testConvertToType() {
        FileSize fileSize = new FileSize();
        fileSize.setFileSystem("fileSystem")
                .setParentDir("parentDir")
                .setFilePath("filePath")
                .setFileName("fileName")
                .setFileSize(1)
                .setIsDir(1)
                .setScanTime(1);
        TableRecordInfo tableRecordInfo = new TableRecordInfo(fileSize);
        System.out.println(tableRecordInfo.convertToSelectSql());
        System.out.println(tableRecordInfo.convertToInsertSql());
    }
}