# URL Shortener

A production-oriented URL shortening service built with **Java 21** and **Spring Boot 4.1**.

The application provides short URL generation, URL redirection, expiration and deactivation support, Redis caching, analytics, access logging, request validation, centralized exception handling, health checks, and automated tests.

---

## Features

- Create shortened URLs
- Generate secure 7-character short codes
- Detect short-code collisions
- Maximum retry protection during short-code generation
- Redirect short URLs to original URLs
- URL expiration support
- URL deactivation support
- Redis-based URL caching
- Cache invalidation for expired/deactivated URLs
- Redis failure fallback to the database
- Redirect count tracking
- Last-access timestamp tracking
- Detailed access logging
  - IP address
  - User-Agent
  - Referrer
  - Access timestamp
- URL metadata API
- URL analytics API
- Recent access analytics API
- Request validation
- Centralized exception handling
- Consistent API error responses
- Generic internal error handling
- Configurable application base URL
- Environment-variable based configuration
- Spring Boot Actuator health and information endpoints
- Unit tests
- Integration tests

---

## Technology Stack

| Technology           | Purpose                            |
| -------------------- | ---------------------------------- |
| Java 21              | Application runtime                |
| Spring Boot 4.1      | Backend framework                  |
| Spring Web MVC       | REST APIs                          |
| Spring Data JPA      | Persistence                        |
| Hibernate            | ORM                                |
| PostgreSQL           | Production database                |
| H2                   | Test database                      |
| Redis                | URL cache                          |
| Flyway               | Database migrations                |
| Maven                | Build and dependency management    |
| JUnit 5              | Testing                            |
| Mockito              | Unit testing                       |
| MockMvc              | Controller integration testing     |
| Spring Boot Actuator | Health and application information |

---

## Architecture

```text
                         Client
                           |
                           v
                +----------------------+
                |   Spring Boot API    |
                |      Controllers     |
                +----------+-----------+
                           |
                           v
                +----------------------+
                |      UrlService      |
                +----------+-----------+
                           |
              +------------+-------------+
              |                          |
              v                          v
      +---------------+          +---------------+
      |  PostgreSQL   |          |     Redis     |
      | Source of     |          |     Cache     |
      | Truth         |          |               |
      +-------+-------+          +---------------+
              |
              v
      +-------------------+
      |   URL Access Logs |
      |-------------------|
      | IP Address        |
      | User-Agent        |
      | Referrer          |
      | Access Timestamp  |
      +-------------------+
```

---

## Project Structure

```text
url-shortener/
│
├── .mvn/
│   └── wrapper/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/url_shortener/
│   │   │       ├── cache/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── domain/
│   │   │       ├── dto/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       └── util/
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── db/
│   │           └── migration/
│   │
│   └── test/
│       ├── java/
│       └── resources/
│
├── .env.example
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

# API Endpoints

## 1. Create Short URL

### Request

```http
POST /api/v1/urls
Content-Type: application/json
```

### Body

```json
{
  "url": "https://www.example.com"
}
```

With expiration:

```json
{
  "url": "https://www.example.com",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

### Response

```http
HTTP/1.1 201 Created
```

```json
{
  "shortCode": "abc1234",
  "shortUrl": "http://localhost:8080/abc1234",
  "originalUrl": "https://www.example.com"
}
```

`expiresAt` is optional.

The URL must start with:

```text
http://
```

or:

```text
https://
```

---

## 2. Redirect Short URL

```http
GET /{shortCode}
```

Example:

```http
GET /abc1234
```

Successful response:

```http
HTTP/1.1 302 Found
Location: https://www.example.com
```

During the redirect operation, the application records:

- Redirect count
- Last accessed timestamp
- IP address
- User-Agent
- Referrer

---

## 3. Get URL Metadata

```http
GET /api/v1/urls/{shortCode}
```

Example:

```http
GET /api/v1/urls/abc1234
```

Example response:

```json
{
  "shortCode": "abc1234",
  "originalUrl": "https://www.example.com",
  "createdAt": "2026-08-08T10:00:00Z",
  "expiresAt": null,
  "active": true,
  "redirectCount": 5,
  "lastAccessedAt": "2026-08-08T11:30:00Z"
}
```

---

## 4. Get URL Analytics

```http
GET /api/v1/urls/{shortCode}/analytics
```

Example:

```http
GET /api/v1/urls/abc1234/analytics
```

Response:

```json
{
  "shortCode": "abc1234",
  "originalUrl": "https://www.example.com",
  "redirectCount": 5,
  "createdAt": "2026-08-08T10:00:00Z",
  "lastAccessedAt": "2026-08-08T11:30:00Z"
}
```

---

## 5. Get Recent Accesses

```http
GET /api/v1/urls/{shortCode}/analytics/accesses
```

This endpoint returns recent access information.

Example response:

```json
[
  {
    "accessedAt": "2026-08-08T11:30:00Z",
    "ipAddress": "127.0.0.1",
    "userAgent": "Mozilla/5.0",
    "referrer": "https://www.google.com"
  }
]
```

The API limits the result to the most recent 100 access records.

---

## 6. Deactivate URL

```http
DELETE /api/v1/urls/{shortCode}
```

Example:

```http
DELETE /api/v1/urls/abc1234
```

Successful response:

```http
HTTP/1.1 204 No Content
```

After deactivation, attempting to redirect the URL returns:

```http
HTTP/1.1 410 Gone
```

---

# Error Handling

The application uses centralized exception handling with `@RestControllerAdvice`.

## Unknown URL

```http
404 Not Found
```

```json
{
  "code": "URL_NOT_FOUND",
  "message": "Short URL not found",
  "timestamp": "2026-08-08T12:00:00Z",
  "fieldErrors": {}
}
```

---

## Expired URL

```http
410 Gone
```

```json
{
  "code": "URL_EXPIRED",
  "message": "Short URL has expired",
  "timestamp": "2026-08-08T12:00:00Z",
  "fieldErrors": {}
}
```

---

## Deactivated URL

```http
410 Gone
```

```json
{
  "code": "URL_DEACTIVATED",
  "message": "Short URL has been deactivated",
  "timestamp": "2026-08-08T12:00:00Z",
  "fieldErrors": {}
}
```

---

## Validation Error

```http
400 Bad Request
```

Example:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "timestamp": "2026-08-08T12:00:00Z",
  "fieldErrors": {
    "url": "URL must start with http:// or https://"
  }
}
```

---

## Unexpected Error

Unexpected application errors are handled centrally and return:

```http
500 Internal Server Error
```

Example:

```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred",
  "timestamp": "2026-08-08T12:00:00Z",
  "fieldErrors": {}
}
```

Internal exception details are intentionally not returned to the client.

---

# Caching

Redis is used as a cache for URL resolution in the `prod` profile.

The application uses a cache abstraction so that the cache implementation can be changed without modifying the URL service.

```text
                         UrlService
                             |
                             v
                          UrlCache
                         /        \
                        /          \
                       v            v
               RedisUrlCache    NoOpUrlCache
                    |                |
                    v                v
                  Redis          No cache
```

In development and test profiles, the no-op cache implementation can be used so the application does not require Redis.

In the production profile, Redis is used for URL caching.

The database remains the source of truth.

If Redis is unavailable, URL resolution falls back to PostgreSQL and the request should still be processed successfully.

## Cache Flow

```text
                 GET /abc1234
                       |
                       v
                     Redis
                    /     \
                  HIT     MISS
                   |        |
                   |        v
                   |    PostgreSQL
                   |        |
                   |        v
                   |      Redis
                   |        |
                   +--------+
                       |
                       v
                 Original URL
                       |
                       v
                  HTTP 302
```

## Cache Invalidation

When a URL is deactivated:

```text
Database
   |
   v
active = false
   |
   v
Redis entry evicted
```

When an expired URL is encountered, the corresponding cache entry is also evicted.

---

# Short Code Generation

Short codes are generated using Java's `SecureRandom`.

The generator currently uses:

```text
0123456789
ABCDEFGHIJKLMNOPQRSTUVWXYZ
abcdefghijklmnopqrstuvwxyz
```

with a code length of:

```text
7 characters
```

This provides:

```text
62^7
```

possible combinations.

The URL service checks for collisions before saving a generated short code.

A maximum retry limit is used to prevent an infinite loop in the unlikely event of repeated collisions.

---

# Data Model

## URL

The `Url` entity stores the primary URL information:

```text
id
shortCode
originalUrl
createdAt
expiresAt
active
redirectCount
lastAccessedAt
```

## URL Access Log

`UrlAccessLog` stores individual redirect/access information:

```text
id
url
accessedAt
ipAddress
userAgent
referrer
```

Access logs are kept separate from the main URL entity so that a URL record does not grow with every redirect.

The relationship is:

```text
Url
 |
 +---- AccessLog
 |
 +---- AccessLog
 |
 +---- AccessLog
 |
 +---- ...
```

---

# Database

The application is designed to use PostgreSQL as the production database.

The test environment uses H2.

Flyway is used to manage database schema changes.

Migration files are located under:

```text
src/main/resources/db/migration
```

Current migrations include:

```text
V1__create_urls_table.sql
V2__create_url_access_logs_table.sql
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This means Hibernate validates the existing database schema instead of creating or modifying tables automatically.

Flyway is responsible for applying database schema changes.

---

# Redis

Redis is used for caching URL resolution in the production profile.

Default local configuration:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Redis is not the source of truth.

If Redis is unavailable, the application falls back to the database.

The development and test profiles can use the no-op cache implementation.

---

# Configuration

Configuration supports environment variables so deployment-specific values do not need to be hard-coded.

Example:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/urlshortener}
spring.datasource.username=${DB_USERNAME:urluser}
spring.datasource.password=${DB_PASSWORD:urlpassword}

spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}

app.base-url=${APP_BASE_URL:http://localhost:8080}
```

## Environment Variables

| Variable       | Purpose                                | Default                                         |
| -------------- | -------------------------------------- | ----------------------------------------------- |
| `DB_URL`       | PostgreSQL JDBC URL                    | `jdbc:postgresql://localhost:5432/urlshortener` |
| `DB_USERNAME`  | Database username                      | `urluser`                                       |
| `DB_PASSWORD`  | Database password                      | `urlpassword`                                   |
| `REDIS_HOST`   | Redis host                             | `localhost`                                     |
| `REDIS_PORT`   | Redis port                             | `6379`                                          |
| `APP_BASE_URL` | Base URL used for generated short URLs | `http://localhost:8080`                         |

For production environments, credentials should be supplied through environment variables or a secrets-management solution rather than committed to source control.

---

# Profiles

The application supports separate development, test, and production configurations.

## Development

The development profile is intended for local development.

When no profile is explicitly supplied, the application uses the default development profile.

```properties
spring.profiles.default=dev
```

Run:

```bash
mvn spring-boot:run
```

or on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

---

## Test

Tests use the `test` profile and H2.

Run:

```bash
mvn clean test
```

---

## Production

The production profile uses PostgreSQL and Redis.

PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

---

# Actuator

Spring Boot Actuator exposes selected operational endpoints:

```text
/actuator/health
/actuator/info
```

## Health

```http
GET /actuator/health
```

Example:

```json
{
  "status": "UP"
}
```

## Info

```http
GET /actuator/info
```

Example:

```json
{
  "app": {
    "name": "URL Shortener",
    "description": "Production-style URL shortening service",
    "version": "0.0.1"
  }
}
```

Only selected actuator endpoints are exposed to avoid unnecessarily exposing operational information.

---

# Validation

Incoming URL creation requests are validated before persistence.

Supported URL formats:

```text
http://example.com
https://example.com
```

Invalid examples:

```text
example.com
www.example.com
not-a-url
```

Expiration timestamps are also validated so that an expiration time in the past is rejected.

---

# Testing

The project contains both unit tests and integration tests.

## Unit Tests

The service layer is tested using:

- JUnit 5
- Mockito

The tests cover scenarios including:

- Short URL creation
- Short-code collision handling
- URL resolution
- Cache hit
- Cache miss
- Redis/cache failure fallback
- Unknown short code
- Expired URL
- Deactivated URL
- Analytics
- URL metadata
- Cache eviction
- Access logging

## Integration Tests

Controller integration tests use:

- Spring Boot Test
- MockMvc
- H2
- Test profile

The integration tests cover:

- URL creation
- URL validation
- Redirects
- Unknown short codes
- Expired URLs
- Deactivated URLs
- Analytics
- URL metadata
- URL deactivation
- Actuator endpoints

---

# Running Tests

Using Maven:

```bash
mvn clean test
```

Using Maven Wrapper on Windows:

```powershell
.\mvnw.cmd clean test
```

Using Maven Wrapper on Linux/macOS:

```bash
./mvnw clean test
```

Expected result:

```text
BUILD SUCCESS
```

---

# Build the Application

Using Maven:

```bash
mvn clean package
```

Using Maven Wrapper on Windows:

```powershell
.\mvnw.cmd clean package
```

Using Maven Wrapper on Linux/macOS:

```bash
./mvnw clean package
```

The generated JAR will be available under:

```text
target/
```

Run the packaged application:

```bash
java -jar target/url-shortener-0.0.1-SNAPSHOT.jar
```

---

# Running Locally

## Prerequisites

- Java 21
- Maven 3.9+ or Maven Wrapper

For development, the application can use the development configuration.

For the production profile, PostgreSQL and Redis are required.

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

---

# Application Configuration

Default application port:

```text
8080
```

Base URL:

```text
http://localhost:8080
```

The base URL can be changed using:

```text
APP_BASE_URL
```

For example:

```text
APP_BASE_URL=https://short.example.com
```

---

# Local Development Flow

```text
1. Start the application
        |
        v
2. Create a short URL
        |
        v
3. Receive generated short code
        |
        v
4. Redirect using the short code
        |
        v
5. Review metadata
        |
        v
6. Review analytics
        |
        v
7. Deactivate URL if required
```

For the production profile:

```text
1. Start PostgreSQL
        |
        v
2. Start Redis
        |
        v
3. Run Flyway migrations
        |
        v
4. Start Spring Boot application
        |
        v
5. Use the URL APIs
```

---

# Example End-to-End Flow

## Create URL

### PowerShell

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/urls" `
  -H "Content-Type: application/json" `
  -d '{"url":"https://www.example.com"}'
```

Example response:

```json
{
  "shortCode": "abc1234",
  "shortUrl": "http://localhost:8080/abc1234",
  "originalUrl": "https://www.example.com"
}
```

---

## Redirect

```powershell
curl.exe -i "http://localhost:8080/abc1234"
```

Expected:

```text
HTTP/1.1 302 Found
Location: https://www.example.com
```

---

## Metadata

```powershell
curl.exe "http://localhost:8080/api/v1/urls/abc1234"
```

---

## Analytics

```powershell
curl.exe "http://localhost:8080/api/v1/urls/abc1234/analytics"
```

---

## Recent Accesses

```powershell
curl.exe "http://localhost:8080/api/v1/urls/abc1234/analytics/accesses"
```

---

## Deactivate

```powershell
curl.exe -X DELETE "http://localhost:8080/api/v1/urls/abc1234"
```

---

# Design Decisions

## PostgreSQL as the Source of Truth

Persistent URL information is stored in PostgreSQL.

Redis is used only as a performance optimization.

This prevents the cache from becoming the authoritative source of URL state.

---

## Separate Access Log Entity

Redirect metadata is stored in a separate `UrlAccessLog` entity.

This prevents the primary URL record from growing indefinitely as redirect traffic increases.

It also allows analytics queries to be added independently.

---

## Cache Invalidation

Cached URLs are evicted when the URL is:

- Deactivated
- Expired

This prevents stale redirects from being served.

---

## Redis Failure Handling

Redis is treated as an optional cache layer rather than the source of truth.

If a Redis read fails, the service falls back to PostgreSQL.

If a Redis write fails after the database operation succeeds, the request is not failed solely because of the cache failure.

This keeps the application available when the cache layer is temporarily unavailable.

---

## Centralized Exception Handling

Expected application exceptions are handled using:

```java
@RestControllerAdvice
```

This provides a consistent error response format across the API.

---

## Configurable Base URL

The generated short URL uses the configured application base URL.

Example:

```text
Development:
http://localhost:8080

Production:
https://short.example.com
```

This avoids hard-coding environment-specific URLs in application logic.

---

## Database Schema Management

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Flyway is responsible for schema migrations.

This prevents Hibernate from silently changing the production database schema.

---

# Production Considerations

The current project provides a foundation for production deployment.

Before deploying to a public environment, additional security and infrastructure controls should be considered, including:

- Authentication and authorization
- API rate limiting
- HTTPS
- Secrets management
- Reverse proxy/load balancer
- Distributed tracing
- Metrics and monitoring
- Centralized logging
- Redis high availability
- PostgreSQL backups
- Database connection pool tuning
- Horizontal application scaling

These are deployment and production concerns and are intentionally kept separate from the core URL-shortening functionality.

---

# Future Improvements

The following are intentionally listed as future improvements and are not presented as currently implemented features:

- Authentication and authorization
- API rate limiting
- OpenAPI/Swagger documentation
- Correlation IDs
- Distributed tracing
- Prometheus metrics
- OpenTelemetry
- Centralized logging
- Advanced analytics
- Unique visitor tracking
- Browser/device analytics
- Custom aliases
- QR code generation
- Message-driven analytics
- Horizontal scaling
- PostgreSQL read replicas
- Redis clustering
- CI/CD pipeline
- Cloud deployment

---

# Docker

Dockerization can be added as a deployment option.

The application can eventually be containerized together with:

```text
Spring Boot
PostgreSQL
Redis
```

Docker setup is kept separate from the core application so the project can also be run directly using Java and Maven during development.

---

# Project Goals

This project demonstrates practical backend engineering concepts including:

- REST API design
- Spring Boot
- Spring Data JPA
- Database persistence
- Redis caching
- Database migrations
- Input validation
- Centralized exception handling
- Entity relationships
- Analytics and access tracking
- Unit testing
- Integration testing
- Configuration management
- Health checks
- Environment-specific configuration
- Cache failure handling
- Production-oriented application design

The focus is on keeping the core implementation simple while providing a structure that can be extended for production-scale requirements.

---

# License

This project is created for educational, demonstration, and interview purposes.
