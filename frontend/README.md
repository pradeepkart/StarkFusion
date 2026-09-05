# StarkFusion Frontend

StarkFusion is a React + Vite + Tailwind career-intelligence interface. It runs immediately in demo mode and is structured to connect to a Spring Boot REST API.

## Run locally

```bash
npm install
npm run dev
```

## Spring Boot connection

Copy `.env.example` to `.env` and set `VITE_API_URL` to your Spring Boot API, normally `http://localhost:8080/api`.

The Axios client automatically adds `Authorization: Bearer <token>` from local storage. The login endpoint is expected to return this shape:

```json
{
  "accessToken": "jwt-token",
  "user": { "id": 1, "name": "Aarav Kumar", "role": "STUDENT" }
}
```

## API contract

| Domain | Endpoints |
| --- | --- |
| Authentication | `POST /auth/login`, `POST /auth/register` |
| Students | `GET/POST /students`, `GET/PUT/DELETE /students/{id}` |
| Student skills | `GET/POST /students/{id}/skills`, `DELETE /students/{id}/skills/{skillId}` |
| Jobs | `GET/POST /jobs`, `GET/PUT/DELETE /jobs/{id}` |
| Applications | `GET/POST /applications`, `PATCH /applications/{id}/status` |
| Intelligence | `GET /skill-gaps`, `GET /students/{id}/recommendations` |

Keep controllers under `/api` in Spring Boot, enable CORS for the Vite origin (`http://localhost:5173`), and secure protected routes with JWT authentication.

## Source layout

- `components/` — reusable visual building blocks, grouped by feature
- `pages/` — screen-level views for students, admins, and authentication
- `services/` — Spring Boot REST API calls; no HTTP requests are scattered through UI components
- `context/` — authenticated user and token lifecycle
- `hooks/` — reusable data and state hooks
- `routes/` — route boundaries and role protection
- `layouts/` — shared student and admin shells
- `utils/` — constants, permissions, and formatting helpers
