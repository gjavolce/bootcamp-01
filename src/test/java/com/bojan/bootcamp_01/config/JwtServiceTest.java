package com.bojan.bootcamp_01.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bojan.bootcamp_01.entity.Role;
import com.bojan.bootcamp_01.entity.User;
import com.bojan.bootcamp_01.service.JwtService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private SecretKey jwtSecretKey;
    @Autowired
    private JwtService jwtService;

    private String subject = "testuser";
    private User testUser;

    @BeforeEach
    void setUp() {
        assertNotNull(jwtProperties.getSecretKey());
        assertNotNull(jwtProperties.getAccessTokenExpiration());
        assertNotNull(jwtProperties.getRefreshTokenExpiration());
        assertNotNull(jwtSecretKey);
        assertNotNull(jwtService);

        // Create a test user with roles
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPasswordHash("hashedpassword");

        Set<Role> roles = new HashSet<>();
        Role userRole = new Role("USER");
        roles.add(userRole);
        testUser.setRoles(roles);
    }

    @Test
    void testJwtServiceAccessTokenGeneration() {
        String deviceId = "device123";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        String accessToken = jwtService.generateAccessToken(testUser, deviceId, ipAddress, userAgent);

        assertNotNull(accessToken);
        assertFalse(accessToken.isEmpty());

        // Verify token structure (JWT has 3 parts separated by dots)
        String[] tokenParts = accessToken.split("\\.");
        assertEquals(3, tokenParts.length);

        // Parse and verify claims
        var claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        assertEquals(testUser.getEmail(), claims.getSubject());
        assertEquals(testUser.getId().toString(), claims.get("user_id"));
        assertEquals(deviceId, claims.get("device_id"));
        assertEquals(ipAddress, claims.get("ip_address"));
        assertEquals(userAgent, claims.get("user_agent"));
        assertEquals("access", claims.get("token_type"));

        // Verify roles
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("USER", roles.get(0));
    }

    @Test
    void testJwtServiceRefreshTokenGeneration() {
        String deviceId = "device123";

        String refreshToken = jwtService.generateRefreshToken(testUser, deviceId);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());

        // Verify token structure
        String[] tokenParts = refreshToken.split("\\.");
        assertEquals(3, tokenParts.length);

        // Parse and verify claims
        var claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        assertEquals(testUser.getEmail(), claims.getSubject());
        assertEquals(testUser.getId().toString(), claims.get("user_id"));
        assertEquals(deviceId, claims.get("device_id"));
        assertEquals("refresh", claims.get("token_type"));

        // Refresh token should not have ip_address and user_agent
        assertNull(claims.get("ip_address"));
        assertNull(claims.get("user_agent"));
    }

    @Test
    void testJwtServiceWithNullUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtService.generateAccessToken(null, "device123", "192.168.1.1", "Mozilla/5.0");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            jwtService.generateRefreshToken(null, "device123");
        });
    }

    @Test
    void testJwtServiceWithUserWithoutRoles() {
        User userWithoutRoles = new User();
        userWithoutRoles.setId(UUID.randomUUID());
        userWithoutRoles.setEmail("noroles@example.com");
        userWithoutRoles.setUsername("noroles");
        userWithoutRoles.setPasswordHash("hashedpassword");
        userWithoutRoles.setRoles(new HashSet<>()); // Empty roles

        String accessToken = jwtService.generateAccessToken(userWithoutRoles, "device123", "192.168.1.1",
                "Mozilla/5.0");

        assertNotNull(accessToken);

        var claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertNotNull(roles);
        assertEquals(0, roles.size());
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
