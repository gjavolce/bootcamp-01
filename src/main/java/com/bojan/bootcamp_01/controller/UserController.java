package com.bojan.bootcamp_01.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bojan.bootcamp_01.dto.UserRegistrationDto;
import com.bojan.bootcamp_01.dto.UserUpdateDto;
import com.bojan.bootcamp_01.entity.User;
import com.bojan.bootcamp_01.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        User user = new User();
        user.setId(null); // Ensure new user
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        // You may want to hash the password here before saving
        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        // createdAt, updatedAt, deletedAt, etc. are handled by the system/DB
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<User> searchUser(@RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        if (username != null) {
            return userRepository.findByUsername(username)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else if (email != null) {
            return userRepository.findByEmail(email)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAllByDeletedAtIsNull());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateDto userUpdateDto) {
        if (userUpdateDto.getUsername() == null || userUpdateDto.getEmail() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        if (userRepository.findByUsername(userUpdateDto.getUsername()).isPresent() &&
            !userRepository.findByUsername(userUpdateDto.getUsername()).get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        if (userRepository.findByEmail(userUpdateDto.getEmail()).isPresent() &&
            !userRepository.findByEmail(userUpdateDto.getEmail()).get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        return userRepository.findById(id)
                .map(existing -> {
                    existing.setUsername(userUpdateDto.getUsername());
                    existing.setEmail(userUpdateDto.getEmail());
                    User updated = userRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setDeletedAt(java.time.Instant.now());
                    userRepository.save(user);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
