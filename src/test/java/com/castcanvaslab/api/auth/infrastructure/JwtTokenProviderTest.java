package com.castcanvaslab.api.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String TEST_SECRET =
            "test-secret-key-that-is-at-least-32-bytes-long-for-hmac-sha256";
    private static final String TEST_ISSUER = "castcanvaslab-test";
    private static final long ACCESS_TOKEN_SECONDS = TimeUnit.HOURS.toSeconds(1);
    private static final long REFRESH_TOKEN_SECONDS = TimeUnit.DAYS.toSeconds(7);

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider =
                new JwtTokenProvider(
                        TEST_SECRET, TEST_ISSUER, ACCESS_TOKEN_SECONDS, REFRESH_TOKEN_SECONDS);
    }

    @Test
    void accessTokenIsValidAndExtractsUserId() {
        UUID userId = UUID.randomUUID();

        String token = jwtTokenProvider.createAccessToken(userId);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validate(token)).isTrue();
        assertThat(jwtTokenProvider.isAccessToken(token)).isTrue();
        assertThat(jwtTokenProvider.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void refreshTokenIsValidButNotAccessType() {
        UUID userId = UUID.randomUUID();

        String token = jwtTokenProvider.createRefreshToken(userId);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validate(token)).isTrue();
        assertThat(jwtTokenProvider.isAccessToken(token)).isFalse();
        assertThat(jwtTokenProvider.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void invalidTokenFailsValidation() {
        assertThat(jwtTokenProvider.validate("invalid.token.value")).isFalse();
    }

    @Test
    void tokenSignedWithDifferentSecretFailsValidation() {
        JwtTokenProvider otherProvider =
                new JwtTokenProvider(
                        "different-secret-key-that-is-at-least-32-bytes-for-hmac",
                        TEST_ISSUER,
                        ACCESS_TOKEN_SECONDS,
                        REFRESH_TOKEN_SECONDS);
        String token = otherProvider.createAccessToken(UUID.randomUUID());

        assertThat(jwtTokenProvider.validate(token)).isFalse();
    }

    @Test
    void expiredTokenFailsValidation() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET, TEST_ISSUER, -1, -1);
        String token = expiredProvider.createAccessToken(UUID.randomUUID());

        assertThat(jwtTokenProvider.validate(token)).isFalse();
    }
}
