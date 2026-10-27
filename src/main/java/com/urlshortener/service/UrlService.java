
package com.urlshortener.service;

import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.repository.UrlRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    private final StringRedisTemplate redisTemplate;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    + "abcdefghijklmnopqrstuvwxyz"
                    + "0123456789";

    private static final SecureRandom RANDOM =
            new SecureRandom();

    public CreateUrlResponse createShortUrl(
            CreateUrlRequest request) {

        String originalUrl = request.getOriginalUrl().trim();

        validateUrl(originalUrl);

        String customAlias = request.getCustomAlias();

        if (customAlias != null) {
            customAlias = customAlias.trim();

            if (customAlias.isEmpty()) {
                customAlias = null;
            }
        }

        String shortCode;

        if (customAlias != null) {
            if (urlRepository.existsByShortCode(customAlias)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This custom alias is already in use"
                );
            }

            shortCode = customAlias;
        } else {
            shortCode = generateUniqueCode();
        }

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setShortCode(shortCode);
        shortUrl.setExpiresAt(request.getExpiresAt());

        urlRepository.save(shortUrl);

        String shortLink =
                baseUrl.replaceAll("/+$", "")
                        + "/" + shortCode;

        return new CreateUrlResponse(
                shortCode,
                shortLink,
                originalUrl,
                shortUrl.getExpiresAt()
        );
    }
    public String getOriginalUrl(String shortCode) {

        String cacheKey = "urlshortener:original:" + shortCode;

        // Step 1: Check Redis
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);

        if (cachedUrl != null) {
            return cachedUrl;
        }

        // Step 2: Find the URL in MySQL
        ShortUrl shortUrl = urlRepository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Short URL not found"
                ));

        // Step 3: Check expiration
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = shortUrl.getExpiresAt();

        if (expiresAt != null && !expiresAt.isAfter(now)) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "This short URL has expired"
            );
        }

        // Step 4: Set the Redis cache duration
        Duration cacheDuration = Duration.ofHours(24);

        if (expiresAt != null) {
            cacheDuration = Duration.between(now, expiresAt);
        }

        // Step 5: Cache the original URL
        redisTemplate.opsForValue().set(
                cacheKey,
                shortUrl.getOriginalUrl(),
                cacheDuration
        );

        // Step 6: Return the original URL
        return shortUrl.getOriginalUrl();
    }

    private void validateUrl(String url) {

        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();

            boolean validScheme =
                    scheme != null
                            && (scheme.equalsIgnoreCase("http")
                            || scheme.equalsIgnoreCase("https"));

            if (!validScheme || uri.getHost() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Enter a valid HTTP or HTTPS URL"
                );
            }

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter a valid URL"
            );
        }
    }

    private String generateUniqueCode() {

        for (int attempt = 0; attempt < 10; attempt++) {

            StringBuilder code = new StringBuilder();

            for (int i = 0; i < 8; i++) {
                int index = RANDOM.nextInt(
                        CHARACTERS.length());

                code.append(CHARACTERS.charAt(index));
            }

            String candidate = code.toString();

            if (!urlRepository.existsByShortCode(candidate)) {
                return candidate;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not generate a unique short code"
        );
    }
}
