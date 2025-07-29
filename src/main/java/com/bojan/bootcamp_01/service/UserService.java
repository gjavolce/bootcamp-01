package com.bojan.bootcamp_01.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bojan.bootcamp_01.dto.UserUpdateDto;
import com.bojan.bootcamp_01.entity.User;
import com.bojan.bootcamp_01.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<User> updateUser(UUID id, UserUpdateDto userUpdateDto) {
        String username = userUpdateDto.getUsername();
        String email = userUpdateDto.getEmail();
        if (username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User existing = userOpt.get();

        Optional<User> usernameUser = userRepository.findByUsername(username.trim());
        if (usernameUser.isPresent() && !usernameUser.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        Optional<User> emailUser = userRepository.findByEmail(email.trim());
        if (emailUser.isPresent() && !emailUser.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        existing.setUsername(username.trim());
        existing.setEmail(email.trim());
        User updated = userRepository.save(existing);
        return ResponseEntity.ok(updated);
    }
}
