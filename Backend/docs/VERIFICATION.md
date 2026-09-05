# Backend verification

Verified on September 5, 2026 using Java 21 and MySQL 8.0.45.

| Check | Result |
|---|---|
| Maven clean verify | BUILD SUCCESS |
| Unit and integration tests | 16 passed, 0 failures, 0 errors |
| Eclipse Java compiler, including unused imports | 0 errors, 0 warnings in main source |
| Concurrent HTTP requests | 77 passed, 0 unexpected responses |
| Maximum dispatched requests in flight | 8 |
| Duplicate application race | 1 created, 7 expected conflicts |

The concurrent checks were run against the final packaged JAR using the separate skill_gap_analyzer_parallel MySQL database. They cover four users, concurrent registration/login and independent skill writes, profile and application isolation, scoring, recommendations, application creation, admin status updates, and expected 400/401/403/404/409 responses. This is functional concurrency verification, not a production capacity benchmark.

See parallel-api-results.json for each request's method, path, status, and expected status. No passwords or JWTs are stored in that report.

Executable JAR SHA-256:

96B49AA49E1FD13D0A71DE96139223768E9B94F7A8921A8A8F1877A408F9B63D

Reproduce with Maven clean verify, then start the server and run scripts/parallel-api-test.ps1. See README.md for exact commands and configuration.
