package com.example.url_shortener.controller;

import com.example.url_shortener.cache.UrlCache;
import com.example.url_shortener.domain.Url;
import com.example.url_shortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UrlRepository urlRepository;

        @MockitoBean
        private UrlCache urlCache;

        @BeforeEach
        void cleanDatabase() {
                urlRepository.deleteAll();
        }

        @Test
        void shouldCreateShortUrl() throws Exception {

                mockMvc.perform(
                                post("/api/v1/urls")
                                                .contentType("application/json")
                                                .content("""
                                                                {
                                                                  "url": "https://www.example.com"
                                                                }
                                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.shortCode").isNotEmpty())
                                .andExpect(jsonPath("$.shortUrl").isNotEmpty())
                                .andExpect(jsonPath("$.originalUrl")
                                                .value("https://www.example.com"));
        }

        @Test
        void shouldRejectInvalidUrl() throws Exception {

                mockMvc.perform(
                                post("/api/v1/urls")
                                                .contentType("application/json")
                                                .content("""
                                                                {
                                                                  "url": "not-a-url"
                                                                }
                                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code")
                                                .value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.message")
                                                .value("Request validation failed"))
                                .andExpect(jsonPath("$.fieldErrors.url")
                                                .value("URL must start with http:// or https://"));
        }

        @Test
        void shouldRedirectToOriginalUrl() throws Exception {

                Url url = new Url(
                                "abc1234",
                                "https://www.example.com",
                                Instant.now(),
                                null);

                urlRepository.save(url);

                // Redis cache miss
                when(urlCache.get("abc1234"))
                                .thenReturn(Optional.empty());

                mockMvc.perform(get("/abc1234"))
                                .andExpect(status().isFound())
                                .andExpect(header().string(
                                                "Location",
                                                "https://www.example.com"));
        }

        @Test
        void shouldReturn404ForUnknownShortCode() throws Exception {

                // Redis cache miss
                when(urlCache.get("doesnotexist"))
                                .thenReturn(Optional.empty());

                mockMvc.perform(get("/doesnotexist"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code")
                                                .value("URL_NOT_FOUND"));
        }

        @Test
        void shouldRejectBlankUrl() throws Exception {

                mockMvc.perform(
                                post("/api/v1/urls")
                                                .contentType("application/json")
                                                .content("""
                                                                {
                                                                  "url": ""
                                                                }
                                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code")
                                                .value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.fieldErrors.url")
                                                .exists());
        }

        @Test
        void shouldReturn410ForExpiredUrl() throws Exception {

                Url expiredUrl = new Url(
                                "expired1",
                                "https://www.example.com",
                                Instant.now().minusSeconds(3600),
                                Instant.now().minusSeconds(60));

                urlRepository.save(expiredUrl);

                // Redis cache miss
                when(urlCache.get("expired1"))
                                .thenReturn(Optional.empty());

                mockMvc.perform(get("/expired1"))
                                .andExpect(status().isGone())
                                .andExpect(jsonPath("$.code")
                                                .value("URL_EXPIRED"));
        }

        @Test
        void shouldDeactivateUrl() throws Exception {

                Url url = new Url(
                                "disable1",
                                "https://www.example.com",
                                Instant.now(),
                                null);

                urlRepository.save(url);

                // Redis should be evicted during deactivation.
                doNothing()
                                .when(urlCache)
                                .evict("disable1");

                // After deactivation, redirect should check the database.
                when(urlCache.get("disable1"))
                                .thenReturn(Optional.empty());

                mockMvc.perform(
                                delete("/api/v1/urls/disable1"))
                                .andExpect(status().isNoContent());

                mockMvc.perform(get("/disable1"))
                                .andExpect(status().isGone())
                                .andExpect(jsonPath("$.code")
                                                .value("URL_DEACTIVATED"));
        }

        @Test
        void shouldRejectPastExpiration() throws Exception {

                mockMvc.perform(
                                post("/api/v1/urls")
                                                .contentType("application/json")
                                                .content("""
                                                                {
                                                                  "url": "https://www.example.com",
                                                                  "expiresAt": "2020-01-01T00:00:00Z"
                                                                }
                                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code")
                                                .value("VALIDATION_ERROR"));
        }

        @Test
        void shouldReturnUrlMetadata() throws Exception {

                Url url = new Url(
                                "abc1234",
                                "https://www.example.com",
                                Instant.now(),
                                null);

                urlRepository.save(url);

                mockMvc.perform(get("/api/v1/urls/abc1234"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.shortCode")
                                                .value("abc1234"))
                                .andExpect(jsonPath("$.originalUrl")
                                                .value("https://www.example.com"))
                                .andExpect(jsonPath("$.active")
                                                .value(true))
                                .andExpect(jsonPath("$.redirectCount")
                                                .value(0))
                                .andExpect(jsonPath("$.createdAt")
                                                .isNotEmpty());
        }

        @Test
        void shouldReturn404ForUnknownMetadataShortCode() throws Exception {

                mockMvc.perform(get("/api/v1/urls/missing"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code")
                                                .value("URL_NOT_FOUND"));
        }
}