package com.hypermall.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypermall.user.dto.*;
import com.hypermall.user.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpoint {

        @Test
        @DisplayName("Should register user successfully")
        void register_WithValidData_ShouldReturnCreated() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("test@example.com");
            request.setPassword("Password1@");
            request.setFullName("Test User");

            AuthResponse response = createAuthResponse();
            when(authService.register(any(RegisterRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refreshToken"));
        }

        @Test
        @DisplayName("Should return bad request for invalid email")
        void register_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("invalid-email");
            request.setPassword("Password1@");
            request.setFullName("Test User");

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return bad request for missing full name")
        void register_WithMissingFullName_ShouldReturnBadRequest() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("test@example.com");
            request.setPassword("Password1@");

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("Should login user successfully")
        void login_WithValidCredentials_ShouldReturnOk() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("Password1@");

            AuthResponse response = createAuthResponse();
            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("accessToken"));
        }

        @Test
        @DisplayName("Should return bad request for missing credentials")
        void login_WithMissingCredentials_ShouldReturnBadRequest() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutEndpoint {

        @Test
        @WithMockUser
        @DisplayName("Should logout user successfully")
        void logout_WithAuthenticatedUser_ShouldReturnOk() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/logout")
                            .with(csrf())
                            .header("Authorization", "Bearer validToken"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return unauthorized for unauthenticated user")
        void logout_WithUnauthenticatedUser_ShouldReturnUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/logout")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh-token")
    class RefreshTokenEndpoint {

        @Test
        @DisplayName("Should refresh token successfully")
        void refreshToken_WithValidToken_ShouldReturnOk() throws Exception {
            // Given
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("validRefreshToken");

            AuthResponse response = createAuthResponse();
            when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/auth/refresh-token")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("accessToken"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/forgot-password")
    class ForgotPasswordEndpoint {

        @Test
        @DisplayName("Should process forgot password request")
        void forgotPassword_WithValidEmail_ShouldReturnOk() throws Exception {
            // Given
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("test@example.com");

            // When & Then
            mockMvc.perform(post("/api/auth/forgot-password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    private AuthResponse createAuthResponse() {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@example.com");
        userResponse.setFullName("Test User");
        userResponse.setRole("BUYER");
        userResponse.setStatus("ACTIVE");

        return AuthResponse.of("accessToken", "refreshToken", 86400L, userResponse);
    }
}
