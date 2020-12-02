package com.czh.util.util;


public class RedisUtilTest {
    RedisUtil redisUtil = new RedisUtil("192.168.20.44", 6379);

    public void testGet() {
    }

    public void testSet() {
        redisUtil.set("fuck", "you", 1000);
    }

    public void testTestSet() {

    }
}