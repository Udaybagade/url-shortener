
package com.urlshortener.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "short_urls",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_short_urls_code",
                        columnNames = "short_code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "short_code", nullable = false, length = 30)
    private String shortCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    public void setCreationTime() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
