package com.bojan.bootcamp_01.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bojan.bootcamp_01.config.JwtProperties;
import com.bojan.bootcamp_01.entity.Role;
import com.bojan.bootcamp_01.entity.User;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties, SecretKey secretKey) {
        this.jwtProperties = jwtProperties;
        this.secretKey = secretKey;
    }

    /**
     * Generates an access token for the given user and device information.
     * 
     * @param user      The user for whom to generate the token
     * @param deviceId  The device identifier
     * @param ipAddress The IP address of the client
     * @param userAgent The user agent string of the client
     * @return The generated JWT access token
     */
    public String generateAccessToken(User user, String deviceId, String ipAddress, String userAgent) {
        if (user == null) {
            logger.error("Cannot generate access token: user is null");
            throw new IllegalArgumentException("User cannot be null");
        }

        try {
            Instant now = Instant.now();
            Instant expiration = now.plusMillis(jwtProperties.getAccessTokenExpiration());

            String[] roles = convertRolesToStringArray(user.getRoles());

            return Jwts.builder()
                    .setSubject(user.getEmail())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiration))
                    .claim("user_id", user.getId().toString())
                    .claim("device_id", deviceId)
                    .claim("ip_address", ipAddress)
                    .claim("user_agent", userAgent)
                    .claim("roles", roles)
                    .claim("token_type", "access")
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();

        } catch (Exception e) {
            logger.error("Error generating access token for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to generate access token", e);
        }
    }

    /**
     * Generates a refresh token for the given user and device.
     * 
     * @param user     The user for whom to generate the token
     * @param deviceId The device identifier
     * @return The generated JWT refresh token
     */
    public String generateRefreshToken(User user, String deviceId) {
        if (user == null) {
            logger.error("Cannot generate refresh token: user is null");
            throw new IllegalArgumentException("User cannot be null");
        }

        try {
            Instant now = Instant.now();
            Instant expiration = now.plusMillis(jwtProperties.getRefreshTokenExpiration());

            String[] roles = convertRolesToStringArray(user.getRoles());

            return Jwts.builder()
                    .setSubject(user.getEmail())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiration))
                    .claim("user_id", user.getId().toString())
                    .claim("device_id", deviceId)
                    .claim("roles", roles)
                    .claim("token_type", "refresh")
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();

        } catch (Exception e) {
            logger.error("Error generating refresh token for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to generate refresh token", e);
        }
    }

    /**
     * Converts a set of Role entities to a String array of role names.
     * 
     * @param roles The set of roles to convert
     * @return Array of role names, or empty array if roles is null or empty
     */
    private String[] convertRolesToStringArray(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new String[0];
        }

        return roles.stream()
                .map(Role::getName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .toArray(String[]::new);
    }
}
