package com.example.url_shortener.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "url_access_logs", indexes = {
        @Index(name = "idx_url_access_logs_url_id", columnList = "url_id"),
        @Index(name = "idx_url_access_logs_accessed_at", columnList = "accessed_at")
})
public class UrlAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "referrer", length = 2048)
    private String referrer;

    protected UrlAccessLog() {
        // Required by JPA
    }

    public UrlAccessLog(
            Url url,
            Instant accessedAt,
            String ipAddress,
            String userAgent,
            String referrer) {

        this.url = url;
        this.accessedAt = accessedAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.referrer = referrer;
    }

    public Long getId() {
        return id;
    }

    public Url getUrl() {
        return url;
    }

    public Instant getAccessedAt() {
        return accessedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getReferrer() {
        return referrer;
    }
}