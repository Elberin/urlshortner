# Risks and Trade-offs

## 1. Correctness Risk

### Risk
A short-code collision could result in an incorrect URL.

### Mitigation
- Random Base62 generation
- Existing-code check
- Database unique constraint

## 2. Data Integrity Risk

### Risk
URL data and analytics could become inconsistent if persistence fails.

### Mitigation
- Transactions where appropriate
- PostgreSQL as source of truth
- Separate access records

## 3. Cache Consistency Risk

### Risk
Redis may contain stale URL data.

### Mitigation
- Evict entries when URLs are deactivated
- Validate URL state before redirect
- PostgreSQL remains authoritative

## 4. Redis Availability Risk

### Risk
Redis may be unavailable.

### Mitigation
Fall back to PostgreSQL.

## 5. Security Risk

### Risk
A URL shortener can be abused to distribute malicious links.

### Current Mitigation
- Validate URL schemes
- Allow only HTTP and HTTPS
- Avoid exposing internal exception details
- Rate limiting

### Future Mitigation
- Abuse detection
- Reputation checking
- Malware scanning
- Authentication

## 6. Rate Limiting Risk

Automated clients may create excessive URLs.

Initial target: 60 requests per minute per client.

## 7. Performance Risk

Database lookups may become expensive as redirect traffic increases.

Mitigation:
- Redis caching
- Database indexes
- Efficient repository queries
- Separate analytics logs

Target: p95 redirect latency below 100 ms under a defined workload.

## 8. Scalability Risk

Access logs can grow faster than URL records.

Future options:
- Partitioning
- Archival
- Aggregation
- Dedicated analytics storage

## 9. Operational Complexity

PostgreSQL, Redis and Docker increase local setup complexity.

Mitigation:
- Environment-specific configuration
- `.env.example`
- Docker Compose
- Clear README

## 10. AI-Generated Code Risk

AI-generated code may contain incorrect assumptions, APIs, missing edge cases or security issues.

Mitigation:
AI output is reviewed, tested and validated by the engineer.

## 11. Scope Risk

The prototype intentionally excludes authentication, frontend UI, Kubernetes, Kafka, multi-region deployment, custom domains and malware scanning.

## 12. Main Trade-offs

### Simplicity vs Production Complexity
Modular monolith instead of microservices.

### Performance vs Infrastructure
Redis improves performance but introduces operational complexity.

### Historical Data vs Storage Growth
Soft deletion preserves history but keeps records.

### Flexibility vs Cache Consistency
Caching improves performance but requires invalidation.
