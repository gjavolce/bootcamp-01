package com.bojan.bootcamp_01.controller;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.bojan.bootcamp_01.dto.UserRegistrationDto;
import com.bojan.bootcamp_01.entity.User;
import com.bojan.bootcamp_01.repository.UserRepository;
import org.springframework.context.annotation.Import;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bojan.bootcamp_01.config.SecurityConfig;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    private User user;
    private UserRegistrationDto userRegistrationDto;

    @BeforeEach
    void setUp() {
        user = new User(null, "testuser", "test@example.com", "hash", false, null, null, 0, null, null, null, null,
                null);
        userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setUsername("testuser");
        userRegistrationDto.setEmail("test@example.com");
        userRegistrationDto.setPassword("password");
    }

    @Test
    void registerUser() throws Exception {
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser"));
    }

    @Test
    void getUserById() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(userRepository.findById(eq(id))).thenReturn(java.util.Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/" + id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser"));
    }

    @Test
    void searchUserByUsername() throws Exception {
        Mockito.when(userRepository.findByUsername(eq("testuser"))).thenReturn(java.util.Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/search")
                .param("username", "testuser"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void searchUserByEmail() throws Exception {
        Mockito.when(userRepository.findByEmail(eq("test@example.com"))).thenReturn(java.util.Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/search")
                .param("email", "test@example.com"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser"));
    }
}