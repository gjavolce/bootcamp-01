package com.bojan.bootcamp_01.entity;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hello_world_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "HelloWorld entity representing greeting messages")
public class HelloWorld {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the HelloWorld message", example = "1")
    private Long id;
    
    @Column(nullable = false)
    @Schema(description = "Name of the person being greeted", example = "John", required = true)
    private String name;
    
    @Column(nullable = false)
    @Schema(description = "The greeting message", example = "Hello, John!", required = true)
    private String message;
    
    @Column(name = "created_at")
    @Schema(description = "Timestamp when the message was created", example = "2023-01-01T12:00:00")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
