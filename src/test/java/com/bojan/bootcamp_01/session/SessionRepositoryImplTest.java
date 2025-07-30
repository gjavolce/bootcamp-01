package com.bojan.bootcamp_01.session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class SessionRepositoryImplTest {
    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:8.0.3-alpine")
            .withExposedPorts(6379);
    private SessionRepositoryImpl sessionRepository;
    private RedisTemplate<String, UserSession> redisTemplate;

    @BeforeEach
    void setUp() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost",
                redisContainer.getMappedPort(6379));
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();
        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        // Set key serializer to String
        redisTemplate.setKeySerializer(new org.springframework.data.redis.serializer.StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new org.springframework.data.redis.serializer.StringRedisSerializer());
        // Set value serializer to JSON for UserSession with JavaTimeModule
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<UserSession> valueSerializer = new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(
                UserSession.class);
        valueSerializer.setObjectMapper(objectMapper);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        redisTemplate.afterPropertiesSet();
        // Clean up all session keys before each test
        Set<String> keys = redisTemplate.keys("session:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        sessionRepository = new SessionRepositoryImpl(redisTemplate);
    }

    @AfterEach
    void cleanUp() {
        // Clean up all session keys after each test to ensure isolation
        Set<String> keys = redisTemplate.keys("session:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void testCreateSession() {
        // Test creating an active session
        UserSession session = new UserSession(1L, "device1", "127.0.0.1", null, null, true);
        sessionRepository.createSession(session);
        UserSession stored = sessionRepository.getSession(1L, "device1");
        assertNotNull(stored);
        assertTrue(stored.isActive());
        assertNotNull(stored.getCreatedAt());
        assertNotNull(stored.getLastActivity());
    }

    @Test
    void testGetSessionFoundAndActive() {
        UserSession session = new UserSession(1L, "device1", "127.0.0.1", Instant.now(), Instant.now(), true);
        sessionRepository.createSession(session);
        UserSession result = sessionRepository.getSession(1L, "device1");
        assertNotNull(result);
        assertTrue(result.isActive());
    }

    @Test
    void testGetSessionNotFound() {
        UserSession result = sessionRepository.getSession(99L, "unknownDevice");
        assertNull(result);
    }

    @Test
    void testUpdateActivityFoundAndActive() {
        UserSession session = new UserSession(1L, "device1", "127.0.0.1", Instant.now(), Instant.now(), true);
        sessionRepository.createSession(session);
        sessionRepository.updateActivity(1L, "device1");
        UserSession updated = sessionRepository.getSession(1L, "device1");
        assertNotNull(updated);
        assertTrue(updated.getLastActivity().isAfter(session.getLastActivity()));
    }

    @Test
    void testUpdateActivityNotFound() {
        sessionRepository.updateActivity(99L, "unknownDevice");
        UserSession result = sessionRepository.getSession(99L, "unknownDevice");
        assertNull(result);
    }

    @Test
    void testGetAllUserSessions() {
        UserSession session1 = new UserSession(1L, "device1", "127.0.0.1", Instant.now(), Instant.now(), true);
        UserSession session2 = new UserSession(1L, "device2", "127.0.0.1", Instant.now(), Instant.now(), true);
        sessionRepository.createSession(session1);
        sessionRepository.createSession(session2);
        List<UserSession> sessions = sessionRepository.getAllUserSessions(1L);
        assertEquals(2, sessions.size());
    }

    @Test
    void testDeleteSession() {
        UserSession session = new UserSession(1L, "device1", "127.0.0.1", Instant.now(), Instant.now(), true);
        sessionRepository.createSession(session);
        sessionRepository.deleteSession(1L, "device1");
        UserSession result = sessionRepository.getSession(1L, "device1");
        assertNull(result);
    }

    @Test
    void testDeleteAllUserSessions() {
        UserSession session1 = new UserSession(1L, "device1", "127.0.0.1", Instant.now(), Instant.now(), true);
        UserSession session2 = new UserSession(1L, "device2", "127.0.0.1", Instant.now(), Instant.now(), true);
        sessionRepository.createSession(session1);
        sessionRepository.createSession(session2);
        sessionRepository.deleteAllUserSessions(1L);
        List<UserSession> sessions = sessionRepository.getAllUserSessions(1L);
        assertEquals(0, sessions.size());
    }

    @Test
    void testGetActiveSessionCount() {
        // Test with no sessions
        assertEquals(0, sessionRepository.getActiveSessionCount());
        
        // Test with one active session
        UserSession activeSession = new UserSession(1L, "device1", "127.0.0.1", Instant.now(), Instant.now(), true);
        sessionRepository.createSession(activeSession);
        assertEquals(1, sessionRepository.getActiveSessionCount());
        
        // Test with one active and one inactive session
        UserSession inactiveSession = new UserSession(2L, "device2", "127.0.0.1", Instant.now(), Instant.now(), false);
        sessionRepository.createSession(inactiveSession);
        assertEquals(1, sessionRepository.getActiveSessionCount());
    }
}
