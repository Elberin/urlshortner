# Assumptions and Engineering Decisions

## 1. Short Code

Generate a random 7-character Base62 short code using a-z, A-Z and 0-9.

A database unique constraint protects against collisions.

## 2. Duplicate URLs

Submitting the same long URL multiple times creates a new short URL each time.

Each short URL has its own lifecycle, expiration and analytics.

## 3. Expiration

Expiration is optional.

If no expiration is provided, the URL does not expire.

An expired URL returns HTTP 410 Gone.

## 4. Deactivation

Deletion is implemented as a soft delete.

The record remains available and `active` becomes `false`.

A deactivated URL returns HTTP 410 Gone when accessed.

## 5. Redirect

Use HTTP 302 Found instead of 301 because the URL can later be expired or deactivated.

## 6. Analytics

A successful redirect records:
- Access timestamp
- IP address
- User-Agent
- Referrer

The URL maintains redirect count and last accessed time.

Detailed access events are stored separately.

## 7. Rate Limiting

Initial target: 60 URL-creation requests per minute per client.

## 8. URL Validation

Only HTTP and HTTPS URLs are supported.

Unsupported schemes include `file://`, `ftp://` and `javascript:`.

## 9. Cache

Redis is used for URL lookups.

PostgreSQL remains the source of truth.

If Redis is unavailable, resolution falls back to PostgreSQL.

## 10. Performance

Initial target: p95 redirect latency below 100 ms under a defined local workload.

## 11. Scope

The prototype does not include authentication, frontend UI, Kubernetes, Kafka, multi-region deployment, custom domains or malware scanning.
