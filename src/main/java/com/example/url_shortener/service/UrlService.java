package com.example.url_shortener.service;

import com.example.url_shortener.cache.UrlCache;
import com.example.url_shortener.domain.Url;
import com.example.url_shortener.domain.UrlAccessLog;
import com.example.url_shortener.dto.CreateUrlRequest;
import com.example.url_shortener.dto.CreateUrlResponse;
import com.example.url_shortener.dto.UrlAccessResponse;
import com.example.url_shortener.dto.UrlAnalyticsResponse;
import com.example.url_shortener.dto.UrlMetadataResponse;
import com.example.url_shortener.exception.UrlDeactivatedException;
import com.example.url_shortener.exception.UrlExpiredException;
import com.example.url_shortener.exception.UrlNotFoundException;
import com.example.url_shortener.repository.UrlAccessLogRepository;
import com.example.url_shortener.repository.UrlRepository;
import com.example.url_shortener.util.ShortCodeGenerator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class UrlService {

        private final UrlCache urlCache;
        private final UrlRepository urlRepository;
        private final ShortCodeGenerator shortCodeGenerator;
        private final UrlAccessLogRepository urlAccessLogRepository;

        private static final int MAX_SHORT_CODE_GENERATION_ATTEMPTS = 10;

        @Value("${app.base-url}")
        private String baseUrl;

        public UrlService(
                        UrlRepository urlRepository,
                        ShortCodeGenerator shortCodeGenerator,
                        UrlCache urlCache,
                        UrlAccessLogRepository urlAccessLogRepository) {

                this.urlRepository = urlRepository;
                this.shortCodeGenerator = shortCodeGenerator;
                this.urlCache = urlCache;
                this.urlAccessLogRepository = urlAccessLogRepository;
        }

        @Transactional
        public CreateUrlResponse createShortUrl(
                        CreateUrlRequest request) {

                String shortCode = null;

                for (int attempt = 0; attempt < MAX_SHORT_CODE_GENERATION_ATTEMPTS; attempt++) {

                        String candidate = shortCodeGenerator.generate();

                        if (!urlRepository
                                        .findByShortCodeAndActiveTrue(candidate)
                                        .isPresent()) {

                                shortCode = candidate;
                                break;
                        }
                }

                if (shortCode == null) {
                        throw new IllegalStateException(
                                        "Unable to generate a unique short code");
                }

                Url url = new Url(
                                shortCode,
                                request.url(),
                                Instant.now(),
                                request.expiresAt());

                Url savedUrl = urlRepository.save(url);

                return new CreateUrlResponse(
                                savedUrl.getShortCode(),
                                baseUrl + "/" + savedUrl.getShortCode(),
                                savedUrl.getOriginalUrl());
        }

        @Transactional
        public String resolveShortCode(String shortCode) {
                return resolveShortCode(
                                shortCode,
                                null,
                                null,
                                null);
        }

        @Transactional
        public String resolveShortCode(
                        String shortCode,
                        String ipAddress,
                        String userAgent,
                        String referrer) {

                Optional<String> cachedUrl;

                // Redis/cache failure should not prevent URL resolution.
                try {
                        cachedUrl = urlCache.get(shortCode);
                } catch (Exception exception) {
                        cachedUrl = Optional.empty();
                }

                // Cache hit
                if (cachedUrl.isPresent()) {

                        Url url = urlRepository
                                        .findByShortCode(shortCode)
                                        .orElseThrow(() -> new UrlNotFoundException(
                                                        "Short URL not found"));

                        // URL was deactivated after the cache entry was created.
                        if (!url.isActive()) {

                                try {
                                        urlCache.evict(shortCode);
                                } catch (Exception exception) {
                                        // Cache eviction failure should not affect the response.
                                }

                                throw new UrlDeactivatedException(
                                                "Short URL has been deactivated");
                        }

                        // URL expired after the cache entry was created.
                        if (isExpired(url)) {

                                try {
                                        urlCache.evict(shortCode);
                                } catch (Exception exception) {
                                        // Cache eviction failure should not affect the response.
                                }

                                throw new UrlExpiredException(
                                                "Short URL has expired");
                        }

                        // Record redirect and access information.
                        recordAccess(
                                        url,
                                        ipAddress,
                                        userAgent,
                                        referrer);

                        return cachedUrl.get();
                }

                // Cache miss or cache unavailable.
                // PostgreSQL is the source of truth.
                Url url = urlRepository
                                .findByShortCode(shortCode)
                                .orElseThrow(() -> new UrlNotFoundException(
                                                "Short URL not found"));

                // Check whether the URL is active.
                if (!url.isActive()) {

                        throw new UrlDeactivatedException(
                                        "Short URL has been deactivated");
                }

                // Check expiration.
                if (isExpired(url)) {

                        throw new UrlExpiredException(
                                        "Short URL has expired");
                }

                // Record redirect and access information.
                recordAccess(
                                url,
                                ipAddress,
                                userAgent,
                                referrer);

                // Cache the URL for future requests.
                // Cache failure must not break the redirect.
                try {
                        urlCache.put(
                                        shortCode,
                                        url.getOriginalUrl());
                } catch (Exception exception) {
                        // Cache failure should not prevent redirect.
                }

                return url.getOriginalUrl();
        }

        @Transactional(readOnly = true)
        public UrlMetadataResponse getUrlMetadata(
                        String shortCode) {

                Url url = urlRepository
                                .findByShortCodeAndActiveTrue(shortCode)
                                .orElseThrow(() -> new UrlNotFoundException(
                                                "Short URL not found"));

                return new UrlMetadataResponse(
                                url.getShortCode(),
                                url.getOriginalUrl(),
                                url.getCreatedAt(),
                                url.getExpiresAt(),
                                url.isActive(),
                                url.getRedirectCount(),
                                url.getLastAccessedAt());
        }

        @Transactional(readOnly = true)
        public UrlAnalyticsResponse getAnalytics(String shortCode) {

                Url url = urlRepository
                                .findByShortCodeAndActiveTrue(shortCode)
                                .orElseThrow(() -> new UrlNotFoundException(
                                                "Short URL not found"));

                return new UrlAnalyticsResponse(
                                url.getShortCode(),
                                url.getOriginalUrl(),
                                url.getRedirectCount(),
                                url.getCreatedAt(),
                                url.getLastAccessedAt());
        }

        @Transactional
        public void deactivateUrl(String shortCode) {

                Url url = urlRepository
                                .findByShortCodeAndActiveTrue(shortCode)
                                .orElseThrow(() -> new UrlNotFoundException(
                                                "Short URL not found"));

                url.deactivate();

                urlRepository.save(url);

                urlCache.evict(shortCode);
        }

        private boolean isExpired(Url url) {

                return url.getExpiresAt() != null
                                && url.getExpiresAt().isBefore(Instant.now());
        }

        private void recordAccess(
                        Url url,
                        String ipAddress,
                        String userAgent,
                        String referrer) {

                url.recordRedirect();

                urlRepository.save(url);

                UrlAccessLog accessLog = new UrlAccessLog(
                                url,
                                Instant.now(),
                                ipAddress,
                                userAgent,
                                referrer);

                urlAccessLogRepository.save(accessLog);
        }

        @Transactional(readOnly = true)
        public java.util.List<UrlAccessResponse> getRecentAccesses(
                        String shortCode) {

                Url url = urlRepository
                                .findByShortCodeAndActiveTrue(shortCode)
                                .orElseThrow(() -> new UrlNotFoundException(
                                                "Short URL not found"));

                return urlAccessLogRepository
                                .findTop100ByUrlOrderByAccessedAtDesc(url)
                                .stream()
                                .map(access -> new UrlAccessResponse(
                                                access.getAccessedAt(),
                                                access.getIpAddress(),
                                                access.getUserAgent(),
                                                access.getReferrer()))
                                .toList();
        }
}