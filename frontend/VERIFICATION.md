# Frontend/backend integration verification

Verified on 2026-09-05 with Java 21, MySQL 8, Node, and Google Chrome.

- Backend `mvnw.cmd clean verify`: 16 tests passed, no failures or errors.
- Frontend `npm run lint`: passed.
- Frontend `npm run build`: passed.
- Frontend `npm run test:e2e`: 3 browser tests passed.
- `Backend/scripts/parallel-api-test.ps1`: 77 API requests passed, up to 8 in flight, against the isolated integration database.

Browser coverage includes administrator login, catalog creation, job requirements, student registration, skill assignment, weighted match (66.67%), recommendations, application creation, administrator status updates, persistence after reload/re-login, bad credentials, malformed local session data, student data isolation, denied administrator access, and invalid-token logout.

Corrections include real API routes and payloads, backend-derived roles, JWT handling, registration/login payload separation, error display, local origin configuration, and production asset imports. Browser reports and screenshots remain in ignored `frontend/.build/`; concurrent API results remain in ignored `Backend/.build/integration-parallel-results.json`.

Run instructions and test prerequisites are documented in `frontend/README.md` and `Backend/README.md`. These results describe the tested local environment; deployment requires its own database, signing key, and origin configuration.
