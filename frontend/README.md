# StarkFusion frontend

React frontend connected to the Spring Boot backend. Authentication, skills, jobs, match scores, recommendations, and applications use the database-backed API.

## Run locally

Start MySQL and the backend first (see [backend instructions](../Backend/README.md)). In a second terminal:

```powershell
cd frontend
npm ci
npm run dev
```

Open http://localhost:5173. Vite forwards `/api` to `http://127.0.0.1:8080`. No environment file is needed for the default setup. To change the backend address, copy `.env.example` to `.env` and change `API_PROXY_TARGET`, then restart Vite.

Sign up to create a student account. The local development administrator is `admin@skillgap.com` / `admin123`, unless overridden in backend configuration. Roles come from the backend login response.

Administrators manage the skill catalog, jobs, requirements, and application statuses. Students manage their skills, review server-calculated gaps and recommendations, and apply for jobs. Refreshing preserves the login; invalid or expired tokens return to sign-in.

## API integration

Paths are relative to `/api`. Axios attaches the JWT to authenticated requests and displays backend validation errors.

| Feature | API |
| --- | --- |
| Register / login | `POST /auth/register`, `POST /auth/login` |
| Current student | `GET /user/profile` |
| Student skills | `GET/POST /user/skills`, `DELETE /user/skills/{skillId}` |
| Jobs / catalog | `GET /jobs`, `GET /skills` |
| Analysis | `GET /user/jobs/{jobId}/skill-gap` |
| Recommendations | `GET /user/jobs/{jobId}/recommendations` |
| Applications | `GET/POST /user/applications` |
| Administrator operations | `/admin/dashboard`, `/admin/students`, `/admin/skills`, `/admin/jobs`, `/admin/applications` |

Login returns `{ token, type, name, email, role }`. Application creation sends `{ jobId }`; the backend derives the student from the JWT and calculates the match percentage.

## Verification

```powershell
npm run lint
npm run build
npm run test:e2e
```

Browser tests require Google Chrome, MySQL, Java 21 on PATH, and the backend JAR built with `Backend/mvnw.cmd clean verify`. Set `JAVA_EXE` to the full Java executable path if necessary. Tests launch an API on port 18082 and frontend on port 4174, and create records in the separate `skill_gap_analyzer_browser_tests` database. Override `E2E_DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` when needed. Reports and screenshots go to `.build/`, excluded from Git.

For deployment, serve `dist/` and proxy `/api` to the backend, or set `VITE_API_URL` before building and configure backend `CORS_ALLOWED_ORIGINS` for the frontend origin. Vite's proxy is not included in static build output.
