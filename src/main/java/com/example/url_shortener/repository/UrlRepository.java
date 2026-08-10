package com.example.url_shortener.repository;

import com.example.url_shortener.domain.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByShortCodeAndActiveTrue(String shortCode);
}