package com.castcanvaslab.api.config;

import com.castcanvaslab.api.auth.domain.RefreshTokenRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRedisConfig {

    @Bean
    public RefreshTokenRepository inMemoryRefreshTokenRepository() {
        return new InMemoryRefreshTokenRepository();
    }

    static class InMemoryRefreshTokenRepository implements RefreshTokenRepository {

        private final Map<UUID, String> store = new ConcurrentHashMap<>();

        @Override
        public void save(UUID userId, String refreshToken, long ttlSeconds) {
            store.put(userId, refreshToken);
        }

        @Override
        public Optional<String> findByUserId(UUID userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public void deleteByUserId(UUID userId) {
            store.remove(userId);
        }
    }
}
