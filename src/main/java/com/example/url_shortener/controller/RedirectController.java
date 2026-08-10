package com.example.url_shortener.controller;

import com.example.url_shortener.service.UrlService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {

    private final UrlService urlService;

    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        String ipAddress = request.getRemoteAddr();

        String userAgent = request.getHeader("User-Agent");

        String referrer = request.getHeader("Referer");

        String originalUrl = urlService.resolveShortCode(
                shortCode,
                ipAddress,
                userAgent,
                referrer);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(
                        HttpHeaders.LOCATION,
                        originalUrl)
                .build();
    }
}