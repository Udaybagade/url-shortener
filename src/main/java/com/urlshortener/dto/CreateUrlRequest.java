
package com.urlshortener.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateUrlRequest {

    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "URL is too long")
    private String originalUrl;

    @Pattern(
            regexp = "[a-zA-Z0-9_-]{3,30}",
            message = "Custom alias must be 3-30 characters "
                    + "using letters, numbers, hyphens or underscores"
    )
    private String customAlias;

    @Future(message = "Expiration time must be in the future")
    private LocalDateTime expiresAt;
}
