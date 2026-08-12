package com.edms.api.controller;

import com.edms.api.dto.AuthResponse;
import com.edms.api.dto.LoginRequest;
import com.edms.api.dto.UserDto;
import com.edms.api.exception.UnauthorizedException;
import com.edms.application.service.AuthApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mysql")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthApplicationService authService;

    @Test
    @DisplayName("POST /auth/login - Success")
    void login_Success() throws Exception {
        UserDto userDto = UserDto.builder()
                .id("u1")
                .email("owner@edms.vn")
                .name("Nguyen Van A")
                .role("OWNER")
                .department("Engineering")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("mock-jwt-token")
                .user(userDto)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        LoginRequest request = new LoginRequest("owner@edms.vn", "Password123!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.user.email").value("owner@edms.vn"));
    }

    @Test
    @DisplayName("POST /auth/login - Unauthorized Invalid Credentials")
    void login_Unauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid email or password"));

        LoginRequest request = new LoginRequest("wrong@edms.vn", "badpass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /auth/login - Validation Error Empty Password")
    void login_ValidationError() throws Exception {
        LoginRequest request = new LoginRequest("owner@edms.vn", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "u1", roles = "OWNER")
    @DisplayName("GET /auth/me - Success")
    void getCurrentUser_Success() throws Exception {
        UserDto userDto = UserDto.builder()
                .id("u1")
                .email("owner@edms.vn")
                .name("Nguyen Van A")
                .role("OWNER")
                .department("Engineering")
                .build();

        when(authService.getCurrentUser(any())).thenReturn(userDto);

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value("u1"));
    }
}
