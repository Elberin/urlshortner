# Architecture and Engineering Decisions

## DEC-001: Random Base62 Short Codes

Generate a random 7-character Base62 code. This provides compact URL-safe identifiers without exposing database IDs.

## DEC-002: PostgreSQL as Source of Truth

PostgreSQL is the authoritative persistent data store because it provides transactions, constraints, indexes and reliable persistence.

## DEC-003: Redis as Cache

Redis caches frequently accessed URL mappings. It improves read performance but is not authoritative.

## DEC-004: Modular Monolith

Use a modular monolith because the prototype focuses on engineering quality and maintainability. Microservices would add unnecessary distributed-system complexity.

## DEC-005: HTTP 302

Use HTTP 302 Found because a 301 can be cached more permanently and the application needs control over URL lifecycle.

## DEC-006: Soft Delete

Deactivation sets `active=false` instead of physically deleting the record, preserving historical information.

## DEC-007: Separate Access Log Entity

Detailed access events are stored separately because they can grow much faster than URL records.

## DEC-008: Environment-Specific Configuration

Development/test can use H2 while production is intended for PostgreSQL and Redis. Deployment-specific values are externalized.

## DEC-009: Global Exception Handling

Use centralized `@RestControllerAdvice` for consistent API errors.

## DEC-010: Automated Validation

Business logic and API behaviour are validated through automated tests to provide regression protection.
