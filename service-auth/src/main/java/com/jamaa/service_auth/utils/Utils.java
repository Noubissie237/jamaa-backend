package com.jamaa.service_auth.utils;

import java.util.Date;

import javax.crypto.SecretKey;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;

@Component
public class Utils {
    
    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(JSONObject customer) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (24 * 60 * 60 * 1000));

        JwtBuilder builder = Jwts.builder()
            .setSubject(customer.getString("email"))
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .claim("id", customer.getLong("id"))
            .claim("email", customer.getString("email"))
            .claim("phone", customer.getString("phone"))
            .claim("lastName", customer.getString("lastName"))
            .claim("firstName", customer.getString("firstName"));

        return builder.compact();
    }
}
