package com.bojan.bootcamp_01.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bojan.bootcamp_01.entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    // Additional query methods if needed
}
