package com.hypermall.common.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;

/**
 * Security headers configuration to add protective HTTP headers to all responses.
 */
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public FilterRegistrationBean<Filter> securityHeadersFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecurityHeadersFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);
        registrationBean.setName("SecurityHeadersFilter");
        return registrationBean;
    }

    /**
     * Filter that adds security headers to HTTP responses.
     */
    private static class SecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Prevent clickjacking
            httpResponse.setHeader("X-Frame-Options", "DENY");

            // XSS Protection
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            // Prevent MIME type sniffing
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // Content Security Policy
            httpResponse.setHeader("Content-Security-Policy",
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: https:; " +
                    "font-src 'self' data:; " +
                    "connect-src 'self' https:; " +
                    "frame-ancestors 'none'");

            // Referrer Policy
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // Permissions Policy
            httpResponse.setHeader("Permissions-Policy",
                    "geolocation=(), " +
                    "microphone=(), " +
                    "camera=(), " +
                    "payment=(), " +
                    "usb=()");

            // HSTS (only on HTTPS)
            String scheme = httpRequest.getScheme();
            if ("https".equalsIgnoreCase(scheme)) {
                httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            }

            chain.doFilter(request, response);
        }
    }
}
