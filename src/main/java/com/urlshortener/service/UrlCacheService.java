
package com.urlshortener.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class UrlCacheService {

    private static final String CACHE_PREFIX = "url:";

    private final StringRedisTemplate redisTemplate;

    public UrlCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String get(String shortCode) {
        return redisTemplate.opsForValue()
                .get(CACHE_PREFIX + shortCode);
    }

    public void put(String shortCode, String originalUrl, Duration ttl) {
        if (ttl == null) {
            // Cache without an expiry only when explicitly intended.
            redisTemplate.opsForValue()
                    .set(CACHE_PREFIX + shortCode, originalUrl);
        } else if (!ttl.isZero() && !ttl.isNegative()) {
            redisTemplate.opsForValue()
                    .set(CACHE_PREFIX + shortCode, originalUrl, ttl);
        }
    }

    public void evict(String shortCode) {
        redisTemplate.delete(CACHE_PREFIX + shortCode);
    }
}
