package com.urlshortener.repository;

import com.urlshortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UrlRepository
        extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime time);
}