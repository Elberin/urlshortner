package com.example.url_shortener.dto;

import java.time.Instant;

public record UrlMetadataResponse(
        String shortCode,
        String originalUrl,
        Instant createdAt,
        Instant expiresAt,
        boolean active,
        long redirectCount,
        Instant lastAccessedAt) {
}