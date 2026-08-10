package com.example.url_shortener.cache;

import java.util.Optional;

public interface UrlCache {

    Optional<String> get(String shortCode);

    void put(String shortCode, String originalUrl);

    void evict(String shortCode);
}