# Ambiguous Scenario

## 1. Ambiguous Requirement

The requirement says the system should support URL expiration but does not completely define the response when an expired URL is accessed.

Question:

> What HTTP response should be returned when a short URL has expired?

## 2. Possible Interpretations

### Interpretation A - 404
Return `404 Not Found`.

### Interpretation B - 410
Return `410 Gone`.

### Interpretation C - Error Page
Redirect to an error page, which would introduce frontend behaviour outside the current API scope.

## 3. Engineering Considerations

The API should distinguish between:
- Unknown URL
- Expired URL
- Deactivated URL

## 4. Decision

Use:
- `404 Not Found` for unknown short codes
- `410 Gone` for expired URLs
- `410 Gone` for deactivated URLs
- `302 Found` for active URLs

## 5. Rationale

404 indicates that the requested resource could not be found.

410 communicates that the resource is known but is no longer available.

## 6. Implementation

```text
Find URL
   |
   +--> Not found -> 404
   |
   +--> Inactive  -> 410
   |
   +--> Expired   -> 410
   |
   +--> Active    -> 302
```

## 7. Validation

| Scenario | Response |
|---|---:|
| Unknown short code | 404 |
| Active URL | 302 |
| Expired URL | 410 |
| Deactivated URL | 410 |

## 8. Final Decision

The API intentionally distinguishes:

```text
404 = never found
410 = previously available but no longer available
302 = active and redirectable
```
