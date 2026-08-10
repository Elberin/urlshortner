package com.example.url_shortener.controller;

import com.example.url_shortener.dto.CreateUrlRequest;
import com.example.url_shortener.dto.CreateUrlResponse;
import com.example.url_shortener.dto.UrlAccessResponse;
import com.example.url_shortener.dto.UrlAnalyticsResponse;
import com.example.url_shortener.dto.UrlMetadataResponse;
import com.example.url_shortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUrlResponse createUrl(
            @Valid @RequestBody CreateUrlRequest request) {

        return urlService.createShortUrl(request);
    }

    @GetMapping("/{shortCode}/analytics")
    public UrlAnalyticsResponse getAnalytics(
            @PathVariable String shortCode) {

        return urlService.getAnalytics(shortCode);
    }

    @DeleteMapping("/{shortCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUrl(
            @PathVariable String shortCode) {

        urlService.deactivateUrl(shortCode);
    }

    @GetMapping("/{shortCode}")
    public UrlMetadataResponse getUrlMetadata(
            @PathVariable String shortCode) {

        return urlService.getUrlMetadata(shortCode);
    }

    @GetMapping("/{shortCode}/analytics/accesses")
    public java.util.List<UrlAccessResponse> getRecentAccesses(
            @PathVariable String shortCode) {

        return urlService.getRecentAccesses(shortCode);
    }
}