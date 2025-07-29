package com.bojan.bootcamp_01.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.bojan.bootcamp_01.entity.Role;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    void testSaveAndFindRole() {
        Role role = new Role("TEST_ROLE");
        Role saved = roleRepository.save(role);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("TEST_ROLE");
        assertThat(roleRepository.findById(saved.getId())).isPresent();
    }
}
