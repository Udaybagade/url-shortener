
package com.urlshortener.service;

import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.repository.UrlRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                urlService,
                "baseUrl",
                "http://localhost:8080"
        );

        lenient().when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);
    }

    @Test
    void createShortUrlShouldSaveUrlAndReturnResponse() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://www.example.com");

        when(urlRepository.existsByShortCode(anyString()))
                .thenReturn(false);

        when(urlRepository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateUrlResponse response = urlService.createShortUrl(request);

        assertNotNull(response);
        assertEquals(
                "https://www.example.com",
                response.getOriginalUrl()
        );
        assertNotNull(response.getShortCode());
        assertTrue(
                response.getShortUrl().startsWith(
                        "http://localhost:8080/"
                )
        );

        verify(urlRepository).save(any(ShortUrl.class));
    }

    @Test
    void createShortUrlShouldSaveCustomAlias() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://www.example.com");
        request.setCustomAlias("myportfolio");

        when(urlRepository.existsByShortCode("myportfolio"))
                .thenReturn(false);

        when(urlRepository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateUrlResponse response = urlService.createShortUrl(request);

        assertEquals("myportfolio", response.getShortCode());
        assertEquals(
                "http://localhost:8080/myportfolio",
                response.getShortUrl()
        );

        verify(urlRepository).save(any(ShortUrl.class));
    }

    @Test
    void createShortUrlShouldRejectDuplicateCustomAlias() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://www.example.com");
        request.setCustomAlias("myportfolio");

        when(urlRepository.existsByShortCode("myportfolio"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> urlService.createShortUrl(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(urlRepository, never()).save(any(ShortUrl.class));
    }

    @Test
    void createShortUrlShouldRejectInvalidUrl() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("not-a-valid-url");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> urlService.createShortUrl(request)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(urlRepository, never()).save(any(ShortUrl.class));
    }

    @Test
    void getOriginalUrlShouldReturnSavedOriginalUrl() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode("abc12345");
        shortUrl.setOriginalUrl("https://www.example.com");
        shortUrl.setExpiresAt(null);

        when(valueOperations.get("urlshortener:original:abc12345"))
                .thenReturn(null);

        when(urlRepository.findByShortCode("abc12345"))
                .thenReturn(Optional.of(shortUrl));

        String originalUrl = urlService.getOriginalUrl("abc12345");

        assertEquals("https://www.example.com", originalUrl);

        verify(urlRepository).findByShortCode("abc12345");
        verify(valueOperations).set(
                "urlshortener:original:abc12345",
                "https://www.example.com",
                Duration.ofHours(24)
        );
    }

    @Test
    void getOriginalUrlShouldReturnCachedUrlWithoutQueryingDatabase() {
        when(valueOperations.get("urlshortener:original:abc12345"))
                .thenReturn("https://www.cached-example.com");

        String originalUrl = urlService.getOriginalUrl("abc12345");

        assertEquals("https://www.cached-example.com", originalUrl);

        verify(urlRepository, never()).findByShortCode(anyString());
        verify(valueOperations, never())
                .set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void getOriginalUrlShouldReturnNotFoundForUnknownCode() {
        when(valueOperations.get("urlshortener:original:unknown"))
                .thenReturn(null);

        when(urlRepository.findByShortCode("unknown"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> urlService.getOriginalUrl("unknown")
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void getOriginalUrlShouldRejectExpiredUrl() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode("expired123");
        shortUrl.setOriginalUrl("https://www.example.com");
        shortUrl.setExpiresAt(LocalDateTime.now().minusMinutes(5));

        when(valueOperations.get("urlshortener:original:expired123"))
                .thenReturn(null);

        when(urlRepository.findByShortCode("expired123"))
                .thenReturn(Optional.of(shortUrl));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> urlService.getOriginalUrl("expired123")
        );

        assertEquals(HttpStatus.GONE, exception.getStatusCode());

        verify(valueOperations, never())
                .set(anyString(), anyString(), any(Duration.class));
    }
}
