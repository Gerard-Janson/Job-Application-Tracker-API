package com.gerard.jobapplicationtracker.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Getter
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateJwtToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey).compact();
    }

    public String getEmailFromJwtToken(String token) {
         return Jwts.parser()
                 .verifyWith(secretKey)
                 .build().parseSignedClaims(token)
                 .getPayload()
                 .getSubject();


    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("JWT validation error :{}",e.getMessage());

        }
        return false;
    }




}
