package com.bojan.bootcamp_01.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

@Repository
public class SessionRepositoryImpl implements SessionRepository {
    private ValueOperations<String, UserSession> ops() {
        return redisTemplate.opsForValue();
    }

    private Set<String> scanKeys(String pattern) {
        // Use Redis SCAN for compatibility with Testcontainers and real Redis
        try {
            Set<String> keys = new java.util.HashSet<>();
            var connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory != null) {
                var connection = connectionFactory.getConnection();
                if (connection != null) {
                    var stringSerializer = redisTemplate.getStringSerializer();
                    try (Cursor<byte[]> cursor = connection
                            .scan(ScanOptions.scanOptions().match(pattern)
                                    .count(1000).build())) {
                        while (cursor.hasNext()) {
                            byte[] rawKey = cursor.next();
                            String key = stringSerializer != null ? (String) stringSerializer.deserialize(rawKey)
                                    : new String(rawKey);
                            keys.add(key);
                        }
                    }
                }
            }
            return keys;
        } catch (Exception e) {
            // Fallback to keys() for environments where scan is not available
            Set<String> keys = redisTemplate.keys(pattern);
            return keys != null ? keys : Collections.emptySet();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SessionRepositoryImpl.class);
    private static final Duration SESSION_TTL = Duration.ofHours(2); // Example sliding TTL
    private final RedisTemplate<String, UserSession> redisTemplate;

    public SessionRepositoryImpl(RedisTemplate<String, UserSession> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getSessionKey(Long userId, String deviceId) {
        return "session:" + userId + ":" + deviceId;
    }

    @Override
    public void createSession(UserSession session) {
        try {
            session.setCreatedAt(Instant.now());
            session.setLastActivity(Instant.now());
            // Preserve the active state that was set when creating the session
            String key = getSessionKey(session.getUserId(), session.getDeviceId());
            ops().set(key, session, SESSION_TTL);
        } catch (DataAccessException e) {
            logger.error("Failed to create session in Redis", e);
        }
    }

    @Override
    public UserSession getSession(Long userId, String deviceId) {
        String key = getSessionKey(userId, deviceId);
        try {
            UserSession session = ops().get(key);
            if (session != null && session.isActive()) {
                session.setLastActivity(Instant.now());
                ops().set(key, session, SESSION_TTL);
                return session;
            }
            return null;
        } catch (DataAccessException e) {
            logger.error("Failed to get session from Redis for userId={}, deviceId={}", userId, deviceId, e);
            return null;
        }
    }

    @Override
    public void updateActivity(Long userId, String deviceId) {
        String key = getSessionKey(userId, deviceId);
        try {
            UserSession session = ops().get(key);
            if (session != null && session.isActive()) {
                session.setLastActivity(Instant.now());
                ops().set(key, session, SESSION_TTL);
            } else {
                logger.warn("Session not found or inactive for userId={}, deviceId={}", userId, deviceId);
            }
        } catch (DataAccessException e) {
            logger.error("Failed to update session activity in Redis for userId={}, deviceId={}", userId, deviceId, e);
        }
    }

    @Override
    public java.util.List<UserSession> getAllUserSessions(Long userId) {
        try {
            String pattern = "session:" + userId + ":*";
            List<UserSession> sessions = new ArrayList<>();
            for (String key : scanKeys(pattern)) {
                UserSession session = ops().get(key);
                if (session != null && session.isActive()) {
                    sessions.add(session);
                }
            }
            return sessions;
        } catch (DataAccessException e) {
            logger.error("Failed to get all sessions for userId={} from Redis", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteSession(Long userId, String deviceId) {
        String key = getSessionKey(userId, deviceId);
        try {
            Boolean result = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(result)) {
                logger.info("Session deleted for userId={}, deviceId={}", userId, deviceId);
            } else {
                logger.warn("Session not found or could not be deleted for userId={}, deviceId={}", userId, deviceId);
            }
        } catch (DataAccessException e) {
            logger.error("Failed to delete session from Redis for userId={}, deviceId={}", userId, deviceId, e);
        }
    }

    @Override
    public void deleteAllUserSessions(Long userId) {
        try {
            String pattern = "session:" + userId + ":*";
            Set<String> keys = scanKeys(pattern);
            if (!keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                logger.info("Deleted {} sessions for userId={}", deleted, userId);
            } else {
                logger.warn("No sessions found to delete for userId={}", userId);
            }
        } catch (DataAccessException e) {
            logger.error("Failed to delete all sessions from Redis for userId={}", userId, e);
        }
    }

    @Override
    public long getActiveSessionCount() {
        try {
            String pattern = "session:*";
            long count = 0;
            for (String key : scanKeys(pattern)) {
                UserSession session = ops().get(key);
                if (session != null && session.isActive()) {
                    count++;
                }
            }
            return count;
        } catch (DataAccessException e) {
            logger.error("Failed to count active sessions in Redis", e);
            return 0;
        }
    }
}
