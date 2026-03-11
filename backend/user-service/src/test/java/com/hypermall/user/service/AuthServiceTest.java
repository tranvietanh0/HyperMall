package com.hypermall.user.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.UnauthorizedException;
import com.hypermall.common.security.JwtTokenProvider;
import com.hypermall.user.dto.*;
import com.hypermall.user.entity.User;
import com.hypermall.user.entity.UserRole;
import com.hypermall.user.entity.UserStatus;
import com.hypermall.user.mapper.UserMapper;
import com.hypermall.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 86400000L);
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604800000L);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .phone("0987654321")
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password1@");
        registerRequest.setFullName("Test User");
        registerRequest.setPhone("0987654321");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password1@");
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void register_WithValidData_ShouldReturnAuthResponse() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByPhone(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtTokenProvider.generateAccessToken(anyString(), anyLong(), anyString()))
                    .thenReturn("accessToken");
            when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(userMapper.toUserResponse(any(User.class))).thenReturn(createUserResponse());

            // When
            AuthResponse response = authService.register(registerRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
            assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_WithExistingEmail_ShouldThrowException() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Email already exists");
        }

        @Test
        @DisplayName("Should throw exception when phone already exists")
        void register_WithExistingPhone_ShouldThrowException() {
            // Given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByPhone(anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Phone number already exists");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_WithValidCredentials_ShouldReturnAuthResponse() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(anyString(), anyLong(), anyString()))
                    .thenReturn("accessToken");
            when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(userMapper.toUserResponse(any(User.class))).thenReturn(createUserResponse());

            // When
            AuthResponse response = authService.login(loginRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void login_WithNonExistentUser_ShouldThrowException() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw exception when password is wrong")
        void login_WithWrongPassword_ShouldThrowException() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw exception when account is inactive")
        void login_WithInactiveAccount_ShouldThrowException() {
            // Given
            testUser.setStatus(UserStatus.INACTIVE);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Account is not active");
        }

        @Test
        @DisplayName("Should throw exception when account is banned")
        void login_WithBannedAccount_ShouldThrowException() {
            // Given
            testUser.setStatus(UserStatus.BANNED);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Account is not active");
        }
    }

    @Nested
    @DisplayName("Reset Password Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password successfully with valid token")
        void resetPassword_WithValidToken_ShouldSucceed() {
            // Given
            testUser.setResetPasswordToken("validToken");
            testUser.setResetPasswordExpires(LocalDateTime.now().plusHours(1));

            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("validToken");
            request.setNewPassword("NewPassword1@");

            when(userRepository.findByResetPasswordToken("validToken"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            authService.resetPassword(request);

            // Then
            verify(userRepository).save(argThat(user ->
                user.getResetPasswordToken() == null &&
                user.getResetPasswordExpires() == null
            ));
        }

        @Test
        @DisplayName("Should throw exception with invalid token")
        void resetPassword_WithInvalidToken_ShouldThrowException() {
            // Given
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("invalidToken");
            request.setNewPassword("NewPassword1@");

            when(userRepository.findByResetPasswordToken("invalidToken"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Invalid or expired reset token");
        }

        @Test
        @DisplayName("Should throw exception with expired token")
        void resetPassword_WithExpiredToken_ShouldThrowException() {
            // Given
            testUser.setResetPasswordToken("expiredToken");
            testUser.setResetPasswordExpires(LocalDateTime.now().minusHours(1));

            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("expiredToken");
            request.setNewPassword("NewPassword1@");

            when(userRepository.findByResetPasswordToken("expiredToken"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Reset token has expired");
        }
    }

    @Nested
    @DisplayName("Email Verification Tests")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should verify email successfully with valid token")
        void verifyEmail_WithValidToken_ShouldSucceed() {
            // Given
            testUser.setVerificationToken("validToken");
            when(userRepository.findByVerificationToken("validToken"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            authService.verifyEmail("validToken");

            // Then
            verify(userRepository).save(argThat(user ->
                user.getEmailVerified() &&
                user.getVerificationToken() == null
            ));
        }

        @Test
        @DisplayName("Should throw exception with invalid token")
        void verifyEmail_WithInvalidToken_ShouldThrowException() {
            // Given
            when(userRepository.findByVerificationToken("invalidToken"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.verifyEmail("invalidToken"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Invalid verification token");
        }
    }

    private UserResponse createUserResponse() {
        return UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .phone("0987654321")
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
