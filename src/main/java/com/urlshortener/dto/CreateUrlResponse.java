
package com.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreateUrlResponse {

    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private LocalDateTime expiresAt;
}
