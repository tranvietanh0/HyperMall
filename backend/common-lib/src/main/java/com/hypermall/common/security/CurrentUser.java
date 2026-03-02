package com.hypermall.common.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Custom annotation to access the currently authenticated user in controllers.
 * Can be used as a method parameter annotation in controller methods.
 *
 * Example usage:
 * <pre>
 * {@code
 * @GetMapping("/me")
 * public ResponseEntity<UserResponse> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
 *     return ResponseEntity.ok(userService.getUserById(currentUser.getId()));
 * }
 * }
 * </pre>
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
