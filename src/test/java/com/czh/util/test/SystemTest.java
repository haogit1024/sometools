package com.czh.util.test;

import com.czh.util.orm.entity.FileSize;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * @author czh
 * 系统测试类
 */
public class SystemTest {
    @Test
    public void testClassPath() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("fuck");
        String ret = stringBuilder.toString();
        System.out.println(ret);
        System.out.println(ret.equals(""));
        System.out.println(StringUtils.isBlank(ret));
        System.out.println(ret.substring(0, ret.length() - 1));
    }
}
