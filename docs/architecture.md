# URL Shortener Architecture

## 1. Architecture Style

The application is a modular monolith using Java 21 and Spring Boot.

## 2. High-Level Architecture

```text
                    Client
                      |
                      v
              +---------------+
              |   Controller  |
              +---------------+
                      |
                      v
              +---------------+
              |    Service    |
              +---------------+
                 |          |
                 v          v
        +-------------+  +-------+
        | Repository  |  | Redis |
        +-------------+  | Cache |
                 |       +-------+
                 v
        +----------------+
        |   PostgreSQL   |
        +----------------+
```

PostgreSQL is the source of truth. Redis is a cache.

## 3. Main Components

### Controller
Receives HTTP requests, performs request-level validation and returns HTTP responses.

### Service
Contains business logic for creating URLs, generating short codes, checking expiration, handling deactivation, redirects and analytics.

### Repository
Provides database access through Spring Data JPA.

### Domain
Main entities are `Url` and `UrlAccessLog`.

### Cache
Stores frequently accessed URL mappings. It is not authoritative.

## 4. Create URL Flow

```text
Client
  |
  | POST /api/v1/urls
  v
Controller
  |
  v
Service
  |
  +--> Generate 7-character Base62 code
  +--> Check uniqueness
  +--> Create Url
  +--> Save
  |
  v
Return short URL
```

## 5. Redirect Flow

```text
Client
  |
  | GET /{shortCode}
  v
RedirectController
  |
  v
UrlService
  |
  +--> Redis lookup
  |
  +--> Cache miss -> PostgreSQL
  |
  v
Validate state
  |
  v
Record redirect
  |
  v
Update cache
  |
  v
HTTP 302
```

## 6. Deactivation Flow

```text
Client
  |
  | DELETE /api/v1/urls/{shortCode}
  v
Controller
  |
  v
Service
  |
  v
PostgreSQL
  |
  +--> active = false
  |
  v
Cache eviction
  |
  v
204 No Content
```

A later redirect returns HTTP 410 Gone.

## 7. Expiration

If `expiresAt` is null, the URL does not expire. If it is before the current time, the URL is expired and returns HTTP 410 Gone.

## 8. Analytics

Successful redirects record access time, IP address, User-Agent and referrer. The URL maintains redirect count and last accessed time.

## 9. Error Handling

A centralized global exception handler provides consistent errors such as:
- URL_NOT_FOUND
- URL_DEACTIVATED
- URL_EXPIRED
- VALIDATION_ERROR
- INTERNAL_SERVER_ERROR

## 10. Configuration

Development/test can use H2. Production is intended for PostgreSQL, Redis and Flyway.

## 11. Testing

Testing includes unit, integration, API and failure-path tests.

## 12. Future Evolution

The modular monolith can later be split into services if scale or organizational requirements justify it.
