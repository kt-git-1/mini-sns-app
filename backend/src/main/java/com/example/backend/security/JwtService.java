package com.example.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final byte[] secretBytes;
    private final long expiresInSec;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expires-in-sec}") long expiresInSec
    ){
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expiresInSec = expiresInSec;
    }

    public String generate(String username, Long userId){
        var now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresInSec)))
                .signWith(Keys.hmacShaKeyFor(secretBytes))
                .compact();
    }

    public String validateAndGetSubject(String token){
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretBytes))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Long extractUserId(String token){
        var claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretBytes))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object uid = claims.get("uid");
        return (uid instanceof Number n) ? n.longValue() : null;
    }
}
