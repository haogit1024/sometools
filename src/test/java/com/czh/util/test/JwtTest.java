package com.czh.util.test;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    private String jwtSecret = "THE_FUCK_SECRET";

    @Test
    public void genJwt() throws InvalidKeyException, NoSuchAlgorithmException {
        Map<String, String> header = new HashMap<String, String>();
        header.put("alg", "HS512");
        Map<String, String> body = new HashMap<>();
        body.put("sub", "czh");
        String headerJson = CodeUtil.toGson(header);
        String bodyJson = CodeUtil.toGson(body);
        String encodedHeader = CodeUtil.base64Encode(headerJson);
        String encodedClaims = CodeUtil.base64Encode(bodyJson);
        String concatenated = encodedHeader + '.' + encodedClaims;
        byte[] bytes = "cy".getBytes();
        HMac mac = new HMac(HmacAlgorithm.HmacSHA512, bytes);
//        String signature = CodeUtil.hmacSHA256(concatenated, "1");
        String signature = mac.digestHex(concatenated);
        System.out.println(concatenated + "." + CodeUtil.base64Encode(signature));
    }

    @Test
    public void testJwt() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String jwt = Jwts.builder().setSubject("czh").signWith(key).compact();
        System.out.println(jwt);
    }

    @Test
    public void testParseJwt() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjemgifQ.3801K7YQBaAWI0HFv97syTN-igsb1gqfT3dPS4BMlT4";
        Jws<Claims> jws;
        jws = Jwts.parser()         // (1)
                .setSigningKey(key)         // (2)
                .parseClaimsJws(jwt); // (3)
        System.out.println(jws);
    }
}
