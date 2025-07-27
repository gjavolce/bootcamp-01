package com.bojan.bootcamp_01.repository;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bojan.bootcamp_01.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class UserRepositoryTest {
    @Container
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
    }

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterAndFindUser() {
        User user = new User(null, "testuser1", "valid.email+test1@example-domain.com", "hash", false, null, null, 0,
                null, null,
                Instant.now(), Instant.now(), null);
        User saved = userRepository.save(user);
        assert saved.getId() != null;
        assert saved.getUsername().equals("testuser1");
        User found = userRepository.findByUsername("testuser1").orElse(null);
        assert found != null;
        assert found.getEmail().equals("valid.email+test1@example-domain.com");
    }

    @Test
    void testUpdateUser() {
        User user = new User(null, "updateuser2", "valid.email+test2@example-domain.com", "hash", false, null, null, 0,
                null, null,
                Instant.now(), Instant.now(), null);
        User saved = userRepository.save(user);
        assert saved != null;
        saved.setUsername("updated2");
        saved.setEmail("valid.email+updated2@example-domain.com");
        saved.setPasswordHash("newhash");
        User updated = userRepository.save(saved);
        assert updated.getUsername().equals("updated2");
        assert updated.getEmail().equals("valid.email+updated2@example-domain.com");
    }

}