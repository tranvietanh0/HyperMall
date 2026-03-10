package com.hypermall.ai.config;

import com.hypermall.common.security.JwtAuthenticationFilter;
import com.hypermall.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, null);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public recommendation endpoints
                        .requestMatchers(HttpMethod.GET, "/api/ai/recommendations/similar/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ai/recommendations/frequently-bought/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ai/recommendations/trending").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ai/recommendations/new-arrivals").permitAll()
                        // Public image search
                        .requestMatchers(HttpMethod.POST, "/api/ai/image-search").permitAll()
                        // Protected endpoints
                        .requestMatchers("/api/ai/chat/**").authenticated()
                        .requestMatchers("/api/ai/recommendations/personalized").authenticated()
                        .requestMatchers("/api/ai/recommendations/track").authenticated()
                        .requestMatchers("/api/ai/image-search/index/**").authenticated()
                        // Actuator and Swagger
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
