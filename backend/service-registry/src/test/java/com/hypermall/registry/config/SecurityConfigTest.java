package com.hypermall.registry.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityConfigTest.TestController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.user.name=registry-user",
        "spring.security.user.password=registry-pass"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowPublicActuatorHealthAndProtectOtherEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/secure").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldBuildRegistryUserFromConfiguredProperties() {
        SecurityConfig securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "username", "registry-user");
        ReflectionTestUtils.setField(securityConfig, "password", "registry-pass");

        InMemoryUserDetailsManager userDetailsService = securityConfig.userDetailsService();

        assertTrue(securityConfig.passwordEncoder().matches(
                "registry-pass",
                userDetailsService.loadUserByUsername("registry-user").getPassword()
        ));
    }

    @RestController
    static class TestController {

        @GetMapping("/actuator/health")
        String health() {
            return "ok";
        }

        @GetMapping("/secure")
        String secure() {
            return "secured";
        }
    }
}
