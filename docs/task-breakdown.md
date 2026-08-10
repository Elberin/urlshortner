# Engineering Task Breakdown

Each stage is implemented, tested and reviewed before moving to the next.

## Phase 1 - Project Setup
- Create Java 21 Spring Boot application
- Configure Maven
- Validate application startup

## Phase 2 - Database
- Configure PostgreSQL
- Create URL table
- Create access-log table
- Add required constraints and migrations

## Phase 3 - Core URL Shortener
- Generate unique 7-character Base62 code
- Implement `POST /api/v1/urls`
- Implement `GET /{shortCode}`

## Phase 4 - URL Lifecycle
- Implement metadata endpoint
- Implement soft deactivation
- Implement expiration

## Phase 5 - Analytics
- Store access information
- Implement analytics endpoint
- Implement access-log endpoint

## Phase 6 - Reliability and Performance
- Add Redis cache
- Add Redis failure fallback
- Add configurable rate limiting

## Phase 7 - Security and Validation
- Validate requests and URLs
- Add global error handling
- Review URL handling, logging and dependencies

## Phase 8 - Testing
- Unit tests
- Integration tests
- Failure tests for invalid, expired, deleted and unknown URLs
- Cache and collision scenarios

## Phase 9 - Documentation
- OpenAPI/Swagger
- Architecture documentation
- AI usage documentation

## Phase 10 - Assignment Scenarios
- Greenfield
- Brownfield
- Ambiguous requirement

## Phase 11 - Final Validation
- Unit tests
- Integration tests
- Static analysis
- Security checks
- Performance validation
- Docker validation
- Final engineering review
