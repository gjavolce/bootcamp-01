package com.bojan.bootcamp_01.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private SecretKey jwtSecretKey;

    private String subject = "testuser";

    @BeforeEach
    void setUp() {
        assertNotNull(jwtProperties.getSecretKey());
        assertNotNull(jwtProperties.getAccessTokenExpiration());
        assertNotNull(jwtProperties.getRefreshTokenExpiration());
        assertNotNull(jwtSecretKey);
    }

    @Test
    void testTokenGenerationAndValidation() {
        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
        assertNotNull(token);

        String parsedSubject = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        assertEquals(subject, parsedSubject);
    }

    @Test
    void testExpiredTokenValidation() throws InterruptedException {
        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 100))
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
        Thread.sleep(150);
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(token);
        });
    }

    @Test
    void testTamperedTokenValidation() {
        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
        String tampered = token.substring(0, token.length() - 2) + "ab";
        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(tampered);
        });
    }

    @Test
    void testMalformedTokenValidation() {
        String malformed = "not.a.jwt.token";
        assertThrows(io.jsonwebtoken.MalformedJwtException.class, () -> {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(malformed);
        });
    }
}
