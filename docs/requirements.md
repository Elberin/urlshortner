# URL Shortener - Requirements

## 1. Objective

Build a URL shortening service that converts long URLs into short, unique URLs and redirects clients to the original URL.

The service will additionally provide:
- URL lifecycle management
- URL expiration
- Access analytics
- Reliability features
- Input validation
- Observability
- Automated testing

## 2. Functional Requirements

### FR-001: Create a shortened URL
`POST /api/v1/urls`

Accept a valid HTTP or HTTPS URL and generate a unique short code.

### FR-002: Redirect using a short code
`GET /{shortCode}`

A valid short code returns HTTP 302 Found with the original URL in the Location header.

### FR-003: Retrieve URL metadata
`GET /api/v1/urls/{shortCode}`

Return short code, original URL, creation time, expiration time, active status, redirect count and last accessed time.

### FR-004: Deactivate a URL
`DELETE /api/v1/urls/{shortCode}`

Use soft deletion and return HTTP 204 No Content.

### FR-005: Support URL expiration
Expiration is optional. Expired URLs return HTTP 410 Gone.

### FR-006: Record access analytics
Record access timestamp, IP address, User-Agent, referrer and associated URL.

### FR-007: Retrieve analytics
`GET /api/v1/urls/{shortCode}/analytics`

Return redirect count and last access information.

Detailed access records:
`GET /api/v1/urls/{shortCode}/analytics/accesses`

## 3. Non-Functional Requirements

- Reliability and graceful failure handling
- Efficient redirect performance
- Data integrity and unique short codes
- Input validation
- Observability
- Testability
- Security and abuse protection

Initial redirect performance target: p95 below 100 ms under a defined local workload.

## 4. Engineering Constraints

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Redis
- Docker / Docker Compose
- Automated testing
- OpenAPI documentation

## 5. AI-Assisted Engineering

AI may assist with requirement analysis, task decomposition, architecture, implementation, debugging, refactoring, tests, documentation and review.

AI output must be reviewed and validated by the engineer.

## 6. Initial Ambiguities

- Short-code length and generation strategy
- Duplicate URL behaviour
- Expiration behaviour
- Analytics definition
- Rate-limit thresholds
- Performance target
- Authentication boundary

## 7. Out of Scope

- User authentication
- User authorization
- Frontend UI
- Kubernetes
- Kafka
- Multi-region deployment
- Custom domains
- Malware/reputation scanning

## 8. Acceptance Criteria

- Valid URLs can be shortened.
- Short URLs redirect using HTTP 302.
- Unknown codes return HTTP 404.
- Expired and deactivated URLs return HTTP 410.
- Metadata and analytics are available.
- Access information is recorded.
- Invalid requests return HTTP 400.
- Cache failures do not prevent URL resolution.
- Automated tests pass.
- Actuator health is available.
- Required documentation is complete.
