package com.czh.util.util;

import com.sun.javaws.security.AppPolicy;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenzh
 * @date 2020/9/18
 */
public class RedisUtil {
    private final int maxConnectionNum = 20;
    private int connectionIndex = 0;
    private final List<Jedis> jedisList = new ArrayList<>(maxConnectionNum);
    public RedisUtil(String host, Integer port) {
        this.init(host, port, null);
    }

    public RedisUtil(String host, Integer port, String password) {
        this.init(host, port, password);
    }

    private void init(String host, Integer port, String password) {
        for (int i = 0; i < maxConnectionNum; i++) {
            Jedis jedis = new Jedis(host, port);
            if (StringUtils.isNotBlank(password)) {
                jedis.auth(password);
            }
            jedisList.add(jedis);
        }
    }

    private Jedis getJedis(String host, Integer port) {
        return getJedis(host, port, "");
    }

    private Jedis getJedis(String host, Integer port, String password) {
        Jedis jedis = new Jedis(host, port);
        if (StringUtils.isNotBlank(password)) {
            jedis.auth(password);
        }
        return jedis;
    }

    private synchronized Jedis getJedis() {
        Jedis jedis = jedisList.get(connectionIndex++);
        if (connectionIndex == jedisList.size()) {
            connectionIndex = 0;
        }
        return jedis;
    }

    public String get(String key) {
        return getJedis().get(key);
    }

    public void set(String key, String value) {
        getJedis().set(key, value);
    }

    public void set(String key, String value, Integer timeOut) {
        getJedis().setex(key, timeOut, value);
    }

    public void close() {
        jedisList.forEach(Jedis::close);
    }
}
