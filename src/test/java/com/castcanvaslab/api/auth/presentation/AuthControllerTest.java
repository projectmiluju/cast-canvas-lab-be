package com.castcanvaslab.api.auth.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.castcanvaslab.api.auth.infrastructure.JwtTokenProvider;
import com.castcanvaslab.api.config.TestRedisConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @Test
    void signupReturnsCreatedWithUserInfo() throws Exception {
        Map<String, String> request =
                Map.of(
                        "email", "test@example.com",
                        "password", "password123",
                        "nickname", "tester");

        mockMvc.perform(
                        post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    void signupDuplicateEmailReturnsConflict() throws Exception {
        Map<String, String> request =
                Map.of(
                        "email", "dup@example.com",
                        "password", "password123",
                        "nickname", "first");

        mockMvc.perform(
                post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(
                        post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_409"));
    }

    @Test
    void signupInvalidEmailReturnsBadRequest() throws Exception {
        Map<String, String> request =
                Map.of(
                        "email", "not-an-email",
                        "password", "password123",
                        "nickname", "tester");

        mockMvc.perform(
                        post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsTokens() throws Exception {
        Map<String, String> signupRequest =
                Map.of(
                        "email", "login@example.com",
                        "password", "password123",
                        "nickname", "loginuser");

        mockMvc.perform(
                post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)));

        Map<String, String> loginRequest =
                Map.of(
                        "email", "login@example.com",
                        "password", "password123");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void loginWrongPasswordReturnsUnauthorized() throws Exception {
        Map<String, String> signupRequest =
                Map.of(
                        "email", "wrong@example.com",
                        "password", "password123",
                        "nickname", "wronguser");

        mockMvc.perform(
                post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)));

        Map<String, String> loginRequest =
                Map.of(
                        "email", "wrong@example.com",
                        "password", "wrongpassword");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401"));
    }

    @Test
    void loginNonExistentUserReturnsUnauthorized() throws Exception {
        Map<String, String> loginRequest =
                Map.of(
                        "email", "noone@example.com",
                        "password", "password123");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401"));
    }

    @Test
    void getMeWithValidTokenReturnsUserInfo() throws Exception {
        Map<String, String> signupRequest =
                Map.of(
                        "email", "me@example.com",
                        "password", "password123",
                        "nickname", "meuser");

        MvcResult signupResult =
                mockMvc.perform(
                                post("/api/v1/auth/signup")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andReturn();

        String userId =
                objectMapper
                        .readTree(signupResult.getResponse().getContentAsString())
                        .get("data")
                        .get("id")
                        .asText();

        String accessToken = jwtTokenProvider.createAccessToken(UUID.fromString(userId));

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("me@example.com"));
    }

    @Test
    void getMeWithoutTokenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isForbidden());
    }

    @Test
    void getMeWithInvalidTokenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshWithValidTokenReturnsNewTokens() throws Exception {
        Map<String, String> signupRequest =
                Map.of(
                        "email", "refresh@example.com",
                        "password", "password123",
                        "nickname", "refreshuser");

        mockMvc.perform(
                post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)));

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        Map.of(
                                                                "email", "refresh@example.com",
                                                                "password", "password123"))))
                        .andReturn();

        String refreshToken =
                objectMapper
                        .readTree(loginResult.getResponse().getContentAsString())
                        .get("data")
                        .get("refreshToken")
                        .asText();

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void refreshWithAccessTokenReturnsUnauthorized() throws Exception {
        Map<String, String> signupRequest =
                Map.of(
                        "email", "refresh2@example.com",
                        "password", "password123",
                        "nickname", "refreshuser2");

        MvcResult signupResult =
                mockMvc.perform(
                                post("/api/v1/auth/signup")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andReturn();

        String userId =
                objectMapper
                        .readTree(signupResult.getResponse().getContentAsString())
                        .get("data")
                        .get("id")
                        .asText();

        String accessToken = jwtTokenProvider.createAccessToken(UUID.fromString(userId));

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("refreshToken", accessToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_REFRESH"));
    }

    @Test
    void refreshWithInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("refreshToken", "invalid.token.value"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_REFRESH"));
    }

    @Test
    void refreshTokenRotationInvalidatesPreviousToken() throws Exception {
        Map<String, String> signupRequest =
                Map.of(
                        "email", "rotation@example.com",
                        "password", "password123",
                        "nickname", "rotationuser");

        mockMvc.perform(
                post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)));

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        Map.of(
                                                                "email", "rotation@example.com",
                                                                "password", "password123"))))
                        .andReturn();

        String originalRefreshToken =
                objectMapper
                        .readTree(loginResult.getResponse().getContentAsString())
                        .get("data")
                        .get("refreshToken")
                        .asText();

        mockMvc.perform(
                post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        Map.of("refreshToken", originalRefreshToken))));

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("refreshToken", originalRefreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_REFRESH"));
    }
}
