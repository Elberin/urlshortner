package com.example.url_shortener.dto;

import java.time.Instant;

public record UrlAnalyticsResponse(
        String shortCode,
        String originalUrl,
        long redirectCount,
        Instant createdAt,
        Instant lastAccessedAt) {
}