package com.castcanvaslab.api.auth.domain;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    void save(UUID userId, String refreshToken, long ttlSeconds);

    Optional<String> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
