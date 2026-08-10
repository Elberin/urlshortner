package com.example.url_shortener.cache;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@Profile("prod")
public class UrlCacheService implements UrlCache {

    private static final String KEY_PREFIX = "url:";

    private final StringRedisTemplate redisTemplate;

    public UrlCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<String> get(String shortCode) {
        String value = redisTemplate.opsForValue()
                .get(KEY_PREFIX + shortCode);

        return Optional.ofNullable(value);
    }

    public void put(String shortCode, String originalUrl) {
        redisTemplate.opsForValue()
                .set(
                        KEY_PREFIX + shortCode,
                        originalUrl,
                        Duration.ofMinutes(30));
    }

    public void evict(String shortCode) {
        redisTemplate.delete(KEY_PREFIX + shortCode);
    }
}