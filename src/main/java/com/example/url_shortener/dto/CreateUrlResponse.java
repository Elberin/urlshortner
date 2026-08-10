package com.example.url_shortener.dto;

public record CreateUrlResponse(
        String shortCode,
        String shortUrl,
        String originalUrl) {
}