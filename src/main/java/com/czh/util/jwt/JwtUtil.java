package com.czh.util.jwt;

import java.util.Date;
import java.util.Map;

public class JwtUtil {
    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";

    public String getUsernameFromToken(String token){
        return null;
    }

    public Date getCreatedDateFromToken(String token) {
        return null;
    }

    private Date getExpirationDateFromToken(String token) {
        return null;
    }

    // TODO 弃用
    private Date generateExpirationDate() {
        return null;
    }

    public Boolean isTokenExpired(String token) {
        return null;
    }

    public String generateToken(String username) {
        return null;
    }

    // TODO 修改为对象传值
    private String generateToken(Map<String, Object> claims) {
        return null;
    }

    /**
     * 判断是否过期
     * @param token
     * @return
     */
    public Boolean canTokenBeRefreshed(String token) {
        return null;
    }

    public String refreshToken(String token) {
        return null;
    }

    public Boolean validateToken(String token, String username) {
        return null;
    }
}
