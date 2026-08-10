CREATE TABLE urls (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(16) NOT NULL,
    original_url VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    redirect_count BIGINT NOT NULL DEFAULT 0,
    last_accessed_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uk_urls_short_code UNIQUE (short_code)
);

CREATE INDEX idx_urls_short_code
    ON urls (short_code);