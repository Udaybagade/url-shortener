
package com.urlshortener.repository;

import com.urlshortener.entity.ShortUrl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class UrlRepositoryTest {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0.44")
                    .withDatabaseName("url_shortener_test")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private UrlRepository urlRepository;

    @Test
    void shouldSaveAndFindUrlByShortCode() {
        ShortUrl url = new ShortUrl();
        url.setOriginalUrl("https://www.example.com");
        url.setShortCode("test1234");

        ShortUrl saved = urlRepository.saveAndFlush(url);

        assertNotNull(saved.getId());

        Optional<ShortUrl> found =
                urlRepository.findByShortCode("test1234");

        assertTrue(found.isPresent());
        assertEquals(
                "https://www.example.com",
                found.get().getOriginalUrl()
        );
    }

    @Test
    void shouldCheckWhetherShortCodeExists() {
        ShortUrl url = new ShortUrl();
        url.setOriginalUrl("https://www.example.com");
        url.setShortCode("exists123");

        urlRepository.saveAndFlush(url);

        assertTrue(urlRepository.existsByShortCode("exists123"));
        assertFalse(urlRepository.existsByShortCode("missing123"));
    }
}
