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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

        @Mock
        private UrlRepository urlRepository;

        @Mock
        private ShortCodeGenerator shortCodeGenerator;

        @Mock
        private UrlCache urlCache;

        @Mock
        private UrlAccessLogRepository urlAccessLogRepository;

        @InjectMocks
        private UrlService urlService;

        @BeforeEach
        void setUp() {

                ReflectionTestUtils.setField(
                                urlService,
                                "baseUrl",
                                "http://localhost:8080");
        }

        @Test
        void shouldCreateShortUrl() {

                Instant expiresAt = Instant.now().plusSeconds(3600);

                when(shortCodeGenerator.generate())
                                .thenReturn("abc1234");

                when(urlRepository
                                .findByShortCodeAndActiveTrue("abc1234"))
                                .thenReturn(Optional.empty());

                Url savedUrl = new Url(
                                "abc1234",
                                "https://example.com",
                                Instant.now(),
                                expiresAt);

                when(urlRepository.save(any(Url.class)))
                                .thenReturn(savedUrl);

                CreateUrlResponse response = urlService.createShortUrl(
                                new CreateUrlRequest(
                                                "https://example.com",
                                                expiresAt));

                assertEquals(
                                "abc1234",
                                response.shortCode());

                assertEquals(
                                "https://example.com",
                                response.originalUrl());

                assertEquals(
                                "http://localhost:8080/abc1234",
                                response.shortUrl());

                verify(urlRepository)
                                .save(any(Url.class));
        }

        @Test
        void shouldResolveExistingUrl() {

                Url url = new Url(
                                "abc1234",
                                "https://example.com",
                                Instant.now(),
                                null);

                when(urlCache.get("abc1234"))
                                .thenReturn(Optional.empty());

                when(urlRepository.findByShortCode("abc1234"))
                                .thenReturn(Optional.of(url));

                String result = urlService.resolveShortCode("abc1234");

                assertEquals(
                                "https://example.com",
                                result);

                assertEquals(
                                1,
                                url.getRedirectCount());

                assertNotNull(
                                url.getLastAccessedAt());

                verify(urlCache)
                                .get("abc1234");

                verify(urlRepository)
                                .findByShortCode("abc1234");

                verify(urlRepository)
                                .save(url);

                verify(urlCache)
                                .put(
                                                "abc1234",
                                                "https://example.com");
        }

        @Test
        void shouldRejectUnknownShortCode() {

                when(urlCache.get("missing"))
                                .thenReturn(Optional.empty());

                when(urlRepository.findByShortCode("missing"))
                                .thenReturn(Optional.empty());

                assertThrows(
                                UrlNotFoundException.class,
                                () -> urlService.resolveShortCode("missing"));

                verify(urlCache)
                                .get("missing");

                verify(urlRepository)
                                .findByShortCode("missing");

                verify(urlRepository, never())
                                .save(any(Url.class));

                verify(urlCache, never())
                                .put(anyString(), anyString());
        }

        @Test
        void shouldRejectExpiredUrl() {

                Instant expiredAt = Instant.now().minusSeconds(60);

                Url expiredUrl = new Url(
                                "expired1",
                                "https://example.com",
                                Instant.now().minusSeconds(3600),
                                expiredAt);

                when(urlCache.get("expired1"))
                                .thenReturn(Optional.empty());

                when(urlRepository.findByShortCode("expired1"))
                                .thenReturn(Optional.of(expiredUrl));

                assertThrows(
                                UrlExpiredException.class,
                                () -> urlService.resolveShortCode("expired1"));

                verify(urlCache)
                                .get("expired1");

                verify(urlRepository)
                                .findByShortCode("expired1");

                verify(urlRepository, never())
                                .save(any(Url.class));

                verify(urlCache, never())
                                .put(anyString(), anyString());
        }

        @Test
        void shouldRejectDeactivatedUrl() {

                Url url = new Url(
                                "disabled1",
                                "https://example.com",
                                Instant.now(),
                                null);

                url.deactivate();

                when(urlCache.get("disabled1"))
                                .thenReturn(Optional.empty());

                when(urlRepository.findByShortCode("disabled1"))
                                .thenReturn(Optional.of(url));

                assertThrows(
                                UrlDeactivatedException.class,
                                () -> urlService.resolveShortCode("disabled1"));

                verify(urlCache)
                                .get("disabled1");

                verify(urlRepository)
                                .findByShortCode("disabled1");

                verify(urlRepository, never())
                                .save(any(Url.class));

                verify(urlCache, never())
                                .put(anyString(), anyString());
        }

        @Test
        void shouldReturnAnalytics() {

                Url url = new Url(
                                "abc1234",
                                "https://example.com",
                                Instant.now(),
                                null);

                url.recordRedirect();
                url.recordRedirect();

                when(urlRepository
                                .findByShortCodeAndActiveTrue("abc1234"))
                                .thenReturn(Optional.of(url));

                UrlAnalyticsResponse response = urlService.getAnalytics("abc1234");

                assertEquals(
                                "abc1234",
                                response.shortCode());

                assertEquals(
                                "https://example.com",
                                response.originalUrl());

                assertEquals(
                                2,
                                response.redirectCount());

                assertNotNull(
                                response.createdAt());

                assertNotNull(
                                response.lastAccessedAt());
        }

        @Test
        void shouldResolveUrlFromCache() {

                Url url = new Url(
                                "abc1234",
                                "https://example.com",
                                Instant.now(),
                                null);

                when(urlCache.get("abc1234"))
                                .thenReturn(
                                                Optional.of(
                                                                "https://example.com"));

                when(urlRepository.findByShortCode("abc1234"))
                                .thenReturn(Optional.of(url));

                String result = urlService.resolveShortCode("abc1234");

                assertEquals(
                                "https://example.com",
                                result);

                assertEquals(
                                1,
                                url.getRedirectCount());

                assertNotNull(
                                url.getLastAccessedAt());

                verify(urlCache)
                                .get("abc1234");

                verify(urlRepository)
                                .findByShortCode("abc1234");

                verify(urlRepository)
                                .save(url);

                verify(urlCache, never())
                                .put(
                                                anyString(),
                                                anyString());
        }

        @Test
        void shouldEvictCacheWhenExpiredUrlIsCached() {

                Instant expiredAt = Instant.now().minusSeconds(60);

                Url expiredUrl = new Url(
                                "expired1",
                                "https://example.com",
                                Instant.now().minusSeconds(3600),
                                expiredAt);

                when(urlCache.get("expired1"))
                                .thenReturn(
                                                Optional.of(
                                                                "https://example.com"));

                when(urlRepository.findByShortCode("expired1"))
                                .thenReturn(Optional.of(expiredUrl));

                assertThrows(
                                UrlExpiredException.class,
                                () -> urlService.resolveShortCode("expired1"));

                verify(urlCache)
                                .evict("expired1");

                verify(urlRepository, never())
                                .save(any(Url.class));
        }

        @Test
        void shouldEvictCacheWhenDeactivatedUrlIsCached() {

                Url url = new Url(
                                "disabled1",
                                "https://example.com",
                                Instant.now(),
                                null);

                url.deactivate();

                when(urlCache.get("disabled1"))
                                .thenReturn(
                                                Optional.of(
                                                                "https://example.com"));

                when(urlRepository.findByShortCode("disabled1"))
                                .thenReturn(Optional.of(url));

                assertThrows(
                                UrlDeactivatedException.class,
                                () -> urlService.resolveShortCode("disabled1"));

                verify(urlCache)
                                .evict("disabled1");

                verify(urlRepository, never())
                                .save(any(Url.class));
        }

        @Test
        void shouldReturnUrlMetadata() {

                Url url = new Url(
                                "abc1234",
                                "https://example.com",
                                Instant.now(),
                                null);

                when(urlRepository
                                .findByShortCodeAndActiveTrue("abc1234"))
                                .thenReturn(Optional.of(url));

                UrlMetadataResponse response = urlService.getUrlMetadata("abc1234");

                assertEquals(
                                "abc1234",
                                response.shortCode());

                assertEquals(
                                "https://example.com",
                                response.originalUrl());

                assertTrue(
                                response.active());

                assertEquals(
                                0,
                                response.redirectCount());

                assertNotNull(
                                response.createdAt());
        }

        @Test
        void shouldFailToReturnMetadataForUnknownUrl() {

                when(urlRepository
                                .findByShortCodeAndActiveTrue("missing"))
                                .thenReturn(Optional.empty());

                assertThrows(
                                UrlNotFoundException.class,
                                () -> urlService.getUrlMetadata("missing"));
        }

        @Test
        void shouldEvictCacheWhenUrlIsDeactivated() {

                Url url = new Url(
                                "abc1234",
                                "https://example.com",
                                Instant.now(),
                                null);

                when(urlRepository
                                .findByShortCodeAndActiveTrue("abc1234"))
                                .thenReturn(Optional.of(url));

                urlService.deactivateUrl("abc1234");

                assertFalse(
                                url.isActive());

                verify(urlRepository)
                                .findByShortCodeAndActiveTrue("abc1234");

                verify(urlRepository)
                                .save(url);

                verify(urlCache)
                                .evict("abc1234");
        }

        @Test
        void shouldFailToDeactivateUnknownUrl() {

                when(urlRepository
                                .findByShortCodeAndActiveTrue("missing"))
                                .thenReturn(Optional.empty());

                assertThrows(
                                UrlNotFoundException.class,
                                () -> urlService.deactivateUrl("missing"));

                verify(urlRepository)
                                .findByShortCodeAndActiveTrue("missing");

                verify(urlRepository, never())
                                .save(any(Url.class));

                verify(urlCache, never())
                                .evict(anyString());
        }

        @Test
        void shouldReturnRecentAccesses() {

                Url url = new Url(
                                "abc1234",
                                "https://example.com",
                                Instant.now(),
                                null);

                UrlAccessLog accessLog = new UrlAccessLog(
                                url,
                                Instant.now(),
                                "127.0.0.1",
                                "Mozilla/5.0",
                                "https://google.com");

                when(urlRepository
                                .findByShortCodeAndActiveTrue("abc1234"))
                                .thenReturn(Optional.of(url));

                when(urlAccessLogRepository
                                .findTop100ByUrlOrderByAccessedAtDesc(url))
                                .thenReturn(List.of(accessLog));

                List<UrlAccessResponse> response = urlService.getRecentAccesses("abc1234");

                assertEquals(1, response.size());

                assertEquals(
                                "127.0.0.1",
                                response.get(0).ipAddress());

                assertEquals(
                                "Mozilla/5.0",
                                response.get(0).userAgent());

                assertEquals(
                                "https://google.com",
                                response.get(0).referrer());

                assertNotNull(
                                response.get(0).accessedAt());
        }

        @Test
        void shouldGenerateAnotherShortCodeWhenCollisionOccurs() {

                Instant expiresAt = Instant.now().plusSeconds(3600);

                when(shortCodeGenerator.generate())
                                .thenReturn("abc1234")
                                .thenReturn("xyz5678");

                when(urlRepository
                                .findByShortCodeAndActiveTrue("abc1234"))
                                .thenReturn(Optional.of(
                                                new Url(
                                                                "abc1234",
                                                                "https://existing.com",
                                                                Instant.now(),
                                                                null)));

                when(urlRepository
                                .findByShortCodeAndActiveTrue("xyz5678"))
                                .thenReturn(Optional.empty());

                Url savedUrl = new Url(
                                "xyz5678",
                                "https://example.com",
                                Instant.now(),
                                expiresAt);

                when(urlRepository.save(any(Url.class)))
                                .thenReturn(savedUrl);

                CreateUrlResponse response = urlService.createShortUrl(
                                new CreateUrlRequest(
                                                "https://example.com",
                                                expiresAt));

                assertEquals(
                                "xyz5678",
                                response.shortCode());

                verify(shortCodeGenerator, times(2))
                                .generate();

                verify(urlRepository)
                                .findByShortCodeAndActiveTrue("abc1234");

                verify(urlRepository)
                                .findByShortCodeAndActiveTrue("xyz5678");

                verify(urlRepository)
                                .save(any(Url.class));
        }

        @Test
        void shouldFailWhenUnableToGenerateUniqueShortCode() {

                when(shortCodeGenerator.generate())
                                .thenReturn("abc1234");

                when(urlRepository
                                .findByShortCodeAndActiveTrue("abc1234"))
                                .thenReturn(Optional.of(
                                                new Url(
                                                                "abc1234",
                                                                "https://existing.com",
                                                                Instant.now(),
                                                                null)));

                IllegalStateException exception = assertThrows(
                                IllegalStateException.class,
                                () -> urlService.createShortUrl(
                                                new CreateUrlRequest(
                                                                "https://example.com",
                                                                null)));

                assertEquals(
                                "Unable to generate a unique short code",
                                exception.getMessage());

                verify(
                                shortCodeGenerator,
                                times(10)).generate();

                verify(
                                urlRepository,
                                never()).save(any(Url.class));
        }

        @Test
        void shouldFallbackToDatabaseWhenCacheIsUnavailable() {

                Url url = new Url(
                                "abc1234",
                                "https://example.com",
                                Instant.now(),
                                null);

                when(urlCache.get("abc1234"))
                                .thenThrow(
                                                new RuntimeException("Redis unavailable"));

                when(urlRepository.findByShortCode("abc1234"))
                                .thenReturn(Optional.of(url));

                String result = urlService.resolveShortCode("abc1234");

                assertEquals(
                                "https://example.com",
                                result);

                assertEquals(
                                1,
                                url.getRedirectCount());

                assertNotNull(
                                url.getLastAccessedAt());

                verify(urlRepository)
                                .findByShortCode("abc1234");

                verify(urlRepository)
                                .save(url);
        }

        @Test
        void shouldRedirectEvenWhenCacheWriteFails() {

                Url url = new Url(
                                "abc1234",
                                "https://example.com",
                                Instant.now(),
                                null);

                when(urlCache.get("abc1234"))
                                .thenReturn(Optional.empty());

                when(urlRepository.findByShortCode("abc1234"))
                                .thenReturn(Optional.of(url));

                doThrow(
                                new RuntimeException("Redis unavailable")).when(urlCache)
                                .put(
                                                "abc1234",
                                                "https://example.com");

                String result = urlService.resolveShortCode("abc1234");

                assertEquals(
                                "https://example.com",
                                result);

                assertEquals(
                                1,
                                url.getRedirectCount());

                verify(urlRepository)
                                .save(url);

                verify(urlCache)
                                .put(
                                                "abc1234",
                                                "https://example.com");
        }
}