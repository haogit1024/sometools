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
        FileSize newSize = new FileSize("2", "2", "2", "2", 2L, 2, 2);
        int ret = orm.insert(newSize);
        System.out.println(ret );
    }

    public void testUpdate() {
        FileSize newSize = new FileSize("2", "2", "2", "2", 3L, 3, 3);
        int ret = orm.update(newSize, "file_system", "file_path");
        System.out.println(ret);
    }

    public void testUpdateById() {
    }

    public void testDelete() {
        FileSize newSize = new FileSize("2", "2", "2", "2", 3L, 3, 3);
        int ret = orm.delete(newSize);
        System.out.println(ret);
    }

    public void testDeleteById() {
    }
}