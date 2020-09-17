package com.czh.util.orm;

import com.czh.util.orm.entity.FileSize;
import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.List;

public class ORMDataBaseTest extends TestCase {
    ORMDataBase orm = new ORMDataBase("db.properties");
    FileSize fileSize = new FileSize("1", null, "1", null, null, null, null);

    public void testSelectOne() throws SQLException {
        fileSize.setFileSystem("1").setFilePath("1");
        FileSize size = orm.selectOne(fileSize);
        System.out.println(size);
    }

    public void testSelectList() throws SQLException {
        List<FileSize> list = orm.selectList(fileSize);
        list.forEach(System.out::println);
    }

    public void testInsert() {
    }

    public void testUpdate() {
    }

    public void testUpdateById() {
    }

    public void testDelete() {
    }

    public void testDeleteById() {
    }
}