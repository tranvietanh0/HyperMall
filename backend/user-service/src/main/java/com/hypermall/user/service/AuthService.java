package com.hypermall.user.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.common.exception.UnauthorizedException;
import com.hypermall.common.security.JwtTokenProvider;
import com.hypermall.user.dto.*;
import com.hypermall.user.entity.User;
import com.hypermall.user.entity.UserStatus;
import com.hypermall.user.mapper.UserMapper;
import com.hypermall.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.jwt.expiration-ms:86400000}")
    private Long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private Long refreshExpirationMs;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already exists");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .verificationToken(UUID.randomUUID().toString())
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        log.info("User logged in: {}", user.getEmail());
        return generateAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        Long userId = jwtTokenProvider.extractUserId(refreshToken);
        String userIdStr = userId != null ? userId.toString() : jwtTokenProvider.extractUsername(refreshToken);
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userIdStr);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new UnauthorizedException("Refresh token not found or expired");
        }

        User user = userRepository.findById(Long.parseLong(userIdStr))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Invalidate old refresh token
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userIdStr);

        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(String accessToken, Long userId) {
        // Blacklist the access token
        long expiration = jwtTokenProvider.extractExpiration(accessToken).getTime();
        long ttl = expiration - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + accessToken,
                    "true",
                    ttl,
                    TimeUnit.MILLISECONDS
            );
        }

        // Remove refresh token
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.info("User logged out: {}", userId);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElse(null);

        // Always return success to prevent email enumeration
        if (user == null) {
            log.warn("Forgot password requested for non-existent email: {}", request.getEmail());
            return;
        }

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordExpires(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // TODO: Send email with reset link
        log.info("Password reset token generated for user: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (user.getResetPasswordExpires() == null ||
                user.getResetPasswordExpires().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpires(null);
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getEmail());
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                user.getId(),
                "ROLE_" + user.getRole().name()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString());

        // Store refresh token in Redis
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                refreshExpirationMs,
                TimeUnit.MILLISECONDS
        );

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtExpirationMs / 1000,
                userMapper.toUserResponse(user)
        );
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
