package com.example.url_shortener.dto;

import java.time.Instant;

public record UrlAccessResponse(
        Instant accessedAt,
        String ipAddress,
        String userAgent,
        String referrer) {
}