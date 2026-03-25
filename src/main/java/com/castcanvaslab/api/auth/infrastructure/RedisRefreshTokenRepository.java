package com.castcanvaslab.api.auth.infrastructure;

import com.castcanvaslab.api.auth.domain.RefreshTokenRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Profile("!test")
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(UUID userId, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(key(userId), refreshToken, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> findByUserId(UUID userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
    }

    @Override
    public void deleteByUserId(UUID userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(UUID userId) {
        return KEY_PREFIX + userId;
    }
}
