CREATE TABLE url_access_logs (
    id BIGSERIAL PRIMARY KEY,
    url_id BIGINT NOT NULL,
    accessed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(1000),
    referrer VARCHAR(2048),

    CONSTRAINT fk_url_access_logs_url
        FOREIGN KEY (url_id)
        REFERENCES urls(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_url_access_logs_url_id
    ON url_access_logs(url_id);

CREATE INDEX idx_url_access_logs_accessed_at
    ON url_access_logs(accessed_at);