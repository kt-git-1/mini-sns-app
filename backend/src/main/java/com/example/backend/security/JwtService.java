package com.example.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiresInSec;

    public JwtService(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.expires-in-sec:86400}") long expiresInSec
    ) {
        byte[] secretBytes = Objects.requireNonNull(secret, "auth.jwt.secret must not be null")
                .getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) { // HS256 requires >= 256 bits
            throw new IllegalArgumentException("auth.jwt.secret must be at least 32 bytes for HS256");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.expiresInSec = expiresInSec;
    }

    /**
     * アクセストークンを発行。
     * SecurityConfig の jwtAuthUserConverter と合わせて
     *  - subject           : username
     *  - claim "user_id"   : Long
     *  - claim "username"  : String
     *  - claim "roles"     : List<String>
     */
    public String generate(String username, Long userId) {
        return generate(username, userId, List.of("USER"));
    }

    public String generate(String username, Long userId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("user_id", userId)
                .claim("username", username)
                .claim("roles", roles == null ? List.of() : roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresInSec)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /** 署名検証して subject(username) を返す */
    public String validateAndGetSubject(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** 署名検証して user_id を取り出す */
    public Long extractUserId(String token) {
        var claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object userId = claims.get("user_id");
        return (userId instanceof Number n) ? n.longValue() : null;
    }
}