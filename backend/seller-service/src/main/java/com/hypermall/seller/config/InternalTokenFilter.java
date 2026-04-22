package com.hypermall.seller.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalTokenFilter extends OncePerRequestFilter {

    private static final String INTERNAL_PATH_PREFIX = "/api/internal/";
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    @Value("${app.internal.token:}")
    private String internalToken;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith(INTERNAL_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestToken = request.getHeader(INTERNAL_TOKEN_HEADER);

        if (internalToken == null || internalToken.isBlank()) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal token is not configured");
            return;
        }

        if (!internalToken.equals(requestToken)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid internal token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
