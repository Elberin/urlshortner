# Greenfield Scenario

## 1. Scenario

The URL shortener was treated as a greenfield application, starting from the initial requirements.

## 2. Requirement Understanding

Primary requirements:
- Create shortened URLs
- Redirect using short codes
- Retrieve metadata
- Deactivate URLs
- Support expiration
- Record access analytics
- Retrieve analytics
- Validate input
- Handle errors
- Provide automated tests

## 3. Task Decomposition

Implementation was divided into:
1. Project setup
2. Database
3. Core URL shortener
4. URL lifecycle
5. Analytics
6. Reliability and performance
7. Security and validation
8. Testing
9. Documentation
10. Assignment scenarios
11. Final validation

## 4. Architecture

A modular monolith was selected:

```text
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Database
```

Redis is used as a cache.

## 5. AI-Assisted Execution

AI assistance was used for requirement analysis, task decomposition, architecture discussion, implementation assistance, debugging, test generation and documentation.

## 6. Engineer Review

Generated suggestions were checked against existing code, DTOs, repositories, Spring Boot behaviour, tests and manual API behaviour.

## 7. Validation

Validation included Maven tests, integration tests and manual PowerShell API testing.

## 8. Result

The greenfield implementation established the core URL shortening, lifecycle and analytics functionality.
