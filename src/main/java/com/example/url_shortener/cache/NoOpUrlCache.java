package com.example.url_shortener.cache;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Profile({"dev", "test"})
public class NoOpUrlCache implements UrlCache {

    @Override
    public Optional<String> get(String shortCode) {
        return Optional.empty();
    }

    @Override
    public void put(String shortCode, String originalUrl) {
        // Cache disabled in dev.
    }

    @Override
    public void evict(String shortCode) {
        // Cache disabled in dev.
    }
}