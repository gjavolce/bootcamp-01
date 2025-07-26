package com.bojan.bootcamp_01.repository;

import com.bojan.bootcamp_01.entity.HelloWorld;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HelloWorldRepository extends JpaRepository<HelloWorld, Long> {
    
    List<HelloWorld> findByNameContainingIgnoreCase(String name);
    
    Optional<HelloWorld> findFirstByNameIgnoreCase(String name);
    
    List<HelloWorld> findTop10ByOrderByCreatedAtDesc();
}
