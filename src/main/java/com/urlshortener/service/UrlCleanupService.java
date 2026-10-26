package com.urlshortener.service;

import com.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlCleanupService {

    private final UrlRepository urlRepository;

    @Scheduled(fixedDelay = 60000)
    public void deleteExpiredUrls() {
        urlRepository.deleteByExpiresAtBefore(
                LocalDateTime.now()
        );
    }
}