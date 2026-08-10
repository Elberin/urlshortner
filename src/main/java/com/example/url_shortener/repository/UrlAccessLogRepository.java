package com.example.url_shortener.repository;

import com.example.url_shortener.domain.Url;
import com.example.url_shortener.domain.UrlAccessLog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UrlAccessLogRepository
                extends JpaRepository<UrlAccessLog, Long> {

        List<UrlAccessLog> findTop100ByUrlOrderByAccessedAtDesc(
                        Url url);
}