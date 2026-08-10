# Testing Strategy

## 1. Objective

Verify business behaviour, API behaviour, persistence, validation, error handling, lifecycle, cache behaviour and regression safety.

## 2. Testing Layers

1. Unit tests
2. Repository/data tests where appropriate
3. Integration tests
4. API tests
5. Edge-case tests
6. Failure-path tests
7. Regression tests

## 3. Unit Tests

Cover:
- Short-code generation
- URL creation
- URL resolution
- Expiration
- Deactivation
- Analytics
- Cache interaction
- Error handling

## 4. Integration Tests

Cover:
- Controller + Service
- Service + Repository
- JPA persistence
- URL lifecycle
- Exception handling

The development/test profile can use H2.

## 5. API Tests

Important endpoints:
- `POST /api/v1/urls`
- `GET /{shortCode}`
- `GET /api/v1/urls/{shortCode}`
- `DELETE /api/v1/urls/{shortCode}`
- `GET /api/v1/urls/{shortCode}/analytics`
- `GET /api/v1/urls/{shortCode}/analytics/accesses`

## 6. Expected HTTP Responses

| Scenario | Expected |
|---|---:|
| Create valid URL | 201 |
| Redirect valid URL | 302 |
| Get metadata | 200 |
| Get analytics | 200 |
| Get access logs | 200 |
| Deactivate URL | 204 |
| Unknown short code | 404 |
| Invalid request | 400 |
| Expired URL | 410 |
| Deactivated URL | 410 |

## 7. Validation Tests

Cover missing URL, blank URL, invalid URL, unsupported schemes, invalid expiration and malformed request bodies.

## 8. Lifecycle Tests

```text
Create -> Active -> Redirect
                  |
                  +-> Deactivate -> 410
```

Expiration:

```text
Create -> Active -> Expiration -> 410
```

## 9. Cache Tests

Verify cache miss, database fallback, cache population, cache hit, cache eviction and Redis-unavailable behaviour.

## 10. Analytics Tests

Verify redirect count, last accessed time and detailed access fields.

## 11. Regression Testing

Run:

```powershell
.\mvnw.cmd clean test
```

after changes.

## 12. Manual API Validation

Manual PowerShell testing covers creation, redirect, metadata, analytics, access logs, deactivation and error paths.

## 13. Quality Gate

- [ ] Tests pass
- [ ] No compilation errors
- [ ] Application starts
- [ ] Core endpoints work
- [ ] Error handling works
- [ ] Validation works
- [ ] No secrets are committed
- [ ] README is complete
- [ ] Documentation is complete
