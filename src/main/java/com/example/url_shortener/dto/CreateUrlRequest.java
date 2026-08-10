package com.example.url_shortener.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateUrlRequest(

                @NotBlank(message = "URL is required") @Size(max = 2048, message = "URL must not exceed 2048 characters") @Pattern(regexp = "https?://.*", message = "URL must start with http:// or https://") String url,

                @Future(message = "Expiration time must be in the future") Instant expiresAt) {
}