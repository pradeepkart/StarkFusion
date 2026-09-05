# Employee / Student Skill Gap Analyzer

A complete REST backend using Java 21, Spring Boot 4.1.1, Spring Web, Spring Data JPA, Spring Security, JWT (JJWT 0.13.0), BCrypt, MySQL, and Maven. No frontend, Docker, or microservices are included.

This version replaces the earlier unauthenticated H2 API. All student-facing routes now use the identity in a verified JWT. The earlier public `/api/students`, write `/api/skills`, write `/api/jobs`, and `/api/applications` routes are not available.

## Requirements and startup

- JDK 21 (Java 17 is not the configured compilation target).
- MySQL 8.0.16+ running locally; tested with MySQL 8.0.45.
- Maven is downloaded by the included wrapper if needed.

PowerShell:

```powershell
cd C:\SkillGapAnalyser\Backend
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'root'
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Base URL: `http://localhost:8080/api`. The server binds to `127.0.0.1` by default.

If Maven resolves its cache to `C:\.m2` in this environment, pass:

```powershell
.\mvnw.cmd "-Dmaven.repo.local=$env:USERPROFILE/.m2/repository" clean verify
.\mvnw.cmd "-Dmaven.repo.local=$env:USERPROFILE/.m2/repository" spring-boot:run
```

Alternatively, run the built executable JAR:

```powershell
& "$env:JAVA_HOME\bin\java.exe" -jar target\Backend-0.0.1-SNAPSHOT.jar
```

## MySQL configuration

`src/main/resources/application.properties` defaults to:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/skill_gap_analyzer?createDatabaseIfNotExist=true}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=${SHOW_SQL:true}
server.port=${PORT:8080}
```

The database is created on first startup if the configured MySQL account has permission. If it does not, create `skill_gap_analyzer` yourself and grant that account access. Hibernate creates the tables and foreign keys. The application does not reset an existing database.

Other configuration:

| Variable | Default | Purpose |
|---|---|---|
| `JWT_SECRET` | Development secret in properties | Signing key; at least 32 UTF-8 bytes |
| `ADMIN_ENABLED` | `true` | Enable development admin bootstrap |
| `ADMIN_NAME` | `Admin` | Initial admin name |
| `ADMIN_EMAIL` | `admin@skillgap.com` | Initial admin email |
| `ADMIN_PASSWORD` | `admin123` | Initial admin password |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated allowed origins |
| `SERVER_ADDRESS` | `127.0.0.1` | Listening interface |
| `SHOW_SQL` | `true` | SQL logging |

Development credentials and the default signing secret are for local testing. Set a private signing secret and admin credentials before exposing the service. Bootstrap creates the admin only if the email does not exist; it never resets an existing password and refuses to promote an existing normal user. Hibernate schema updates are for this project; versioned production migrations are not included.

H2 is a **test-only dependency**. Normal application startup always uses MySQL.

## Database structure

All entity relationships have real foreign keys. Responses use DTOs; neither User entities nor password hashes are returned.

| Table | Fields and relationships |
|---|---|
| `users` | id, name, unique email, BCrypt password, role |
| `students` | student_id, name, unique email, unique non-null user_id → users |
| `skills` | skill_id, unique name, category |
| `student_skills` | id, student_id → students, skill_id → skills, proficiency |
| `jobs` | job_id, company, title, location |
| `job_skills` | id, job_id → jobs, skill_id → skills, required_level, mandatory |
| `applications` | id, student_id → students, job_id → jobs, match_percent, status |
| `recommendations` | id, student_id → students, job_id → jobs, skill_id → skills, priority, reason |

One ROLE_USER account receives exactly one Student profile during registration, atomically. The default admin has no Student profile. Unique pairs prevent repeated student skills, job requirements, and applications. Recommendation triples are also unique.

Emails are trimmed and lowercased. Skill names preserve display capitalization, with case-insensitive duplicate checks. MySQL's configured collation also controls name comparisons.

## Authentication

1. Register with name, email, and a password of at least 6 characters.
2. The server assigns ROLE_USER, hashes the password using BCrypt, and creates a Student profile.
3. Login passes email/password through Spring Security's AuthenticationManager and DaoAuthenticationProvider.
4. A valid login returns a signed JWT with the email as `sub`, role, issued time, and expiry.
5. Send `Authorization: Bearer <token>` on protected requests.
6. The JWT filter verifies signature/expiry, loads current account details, verifies the token's role against the database, and establishes the SecurityContext.

Tokens last 24 hours (`jwt.expiration=86400000`). Invalid, expired, unsigned, wrong-signature, missing-expiration, or stale-role tokens return 401. No HTTP session is created. There is no refresh-token or logout endpoint; tokens expire after 24 hours.

BCrypt accepts at most 72 UTF-8 bytes. Registration enforces that byte limit in addition to the character validation.

### Register

`POST /api/auth/register` — public, returns 201.

```json
{
  "name": "Pradeep",
  "email": "pradeep@gmail.com",
  "password": "123456"
}
```

```json
{ "message": "User registered successfully" }
```

Do not send `role`: unknown request fields are rejected with 400. Public registration cannot create an administrator.

### Login

`POST /api/auth/login` — public, returns 200.

```json
{ "email": "pradeep@gmail.com", "password": "123456" }
```

```json
{
  "token": "<JWT>",
  "type": "Bearer",
  "name": "Pradeep",
  "email": "pradeep@gmail.com",
  "role": "ROLE_USER"
}
```

Development admin login: `admin@skillgap.com` / `admin123`, unless overridden at bootstrap.

## Authorization

| Routes | Access |
|---|---|
| POST `/api/auth/register`, POST `/api/auth/login` | Public |
| `/api/admin/**` | ROLE_ADMIN only |
| `/api/user/**` | ROLE_USER only |
| GET `/api/jobs/**`, GET `/api/skills/**` | Either authenticated role |
| Other routes | Denied |

An unauthenticated protected request receives 401. An authenticated wrong-role request receives 403, including an admin calling the user-only API.

User routes never accept a studentId. The verified JWT email is resolved to User → Student. User application lists and skill mutations are scoped to that profile. Requests containing an injected `studentId`, `matchPercent`, or `role` are rejected.

CORS supports `http://localhost:5173`, GET/POST/PUT/DELETE/OPTIONS, and Authorization/Content-Type headers. Browser preflight runs before authentication.

## Admin APIs

All paths below start with `/api/admin` and require an admin Bearer token.

| Method | Path | Purpose |
|---|---|---|
| GET | `/dashboard` | Counts and average application match |
| GET | `/students` | All student profiles |
| GET | `/students/{studentId}` | Student details |
| GET | `/students/{studentId}/skills` | Student's current skills |
| POST | `/skills` | Create skill, 201 |
| GET | `/skills` | All skills |
| PUT | `/skills/{skillId}` | Update name/category |
| DELETE | `/skills/{skillId}` | Delete unused skill, 204 |
| POST | `/jobs` | Create job, 201 |
| GET | `/jobs` | All jobs |
| GET | `/jobs/{jobId}` | Job details |
| PUT | `/jobs/{jobId}` | Update company/title/location |
| DELETE | `/jobs/{jobId}` | Delete job without applications, 204 |
| POST | `/jobs/{jobId}/skills` | Add/update one requirement |
| GET | `/jobs/{jobId}/skills` | Required skills |
| DELETE | `/jobs/{jobId}/skills/{skillId}` | Remove requirement, 204 |
| GET | `/applications` | All applications, newest first |
| PUT | `/applications/{applicationId}/status` | Change application status |

Skill body:

```json
{ "name": "Spring Boot", "category": "Backend" }
```

Job body:

```json
{ "company": "ABC Technologies", "title": "Java Developer", "location": "Chennai" }
```

Job requirement body:

```json
{ "skillId": 1, "requiredLevel": 4, "mandatory": true }
```

Status body:

```json
{ "status": "SHORTLISTED" }
```

Allowed statuses: `APPLIED`, `SHORTLISTED`, `REJECTED`, `SELECTED`. Admin can set any listed status; the specification does not define transition restrictions.

Dashboard returns `totalStudents`, `totalJobs`, `totalApplications`, and `averageSkillMatch`. Average is computed across stored application match snapshots, rounded to two decimals; no applications yields 0.00.

A skill referenced by student/job assignments returns 409 on deletion. Remove its assignments first. A job with applications returns 409 on deletion to preserve application history. Deleting an unused job removes its requirements and recommendations in the same transaction.

## User APIs

All paths below start with `/api/user` and require a user Bearer token.

| Method | Path | Purpose |
|---|---|---|
| GET | `/profile` | Current student's profile |
| GET | `/skills` | Current student's skills |
| POST | `/skills` | Add/update proficiency |
| DELETE | `/skills/{skillId}` | Remove own skill, 204 |
| GET | `/jobs` | Available jobs |
| GET | `/jobs/{jobId}` | Job details |
| GET | `/jobs/{jobId}/skill-gap` | Live skill comparison |
| GET | `/jobs/{jobId}/recommendations` | Prioritized recommendations |
| POST | `/applications` | Apply to a job, 201 |
| GET | `/applications` | Own applications only |

Student skill body:

```json
{ "skillId": 1, "proficiency": 4 }
```

Application body:

```json
{ "jobId": 1 }
```

Both roles can also GET `/api/skills`, `/api/skills/{skillId}`, `/api/jobs`, `/api/jobs/{jobId}`, and `/api/jobs/{jobId}/skills`.

List endpoints return arrays. IDs are generated; use returned IDs rather than assuming 1.

## Skill gap and recommendations

Proficiency and required levels are integers **1–5**: Beginner, Basic, Intermediate, Advanced, Expert. A skill absent from the profile is treated as level 0 during calculation.

```text
gap = max(requiredLevel - currentLevel, 0)
status = currentLevel >= requiredLevel ? MATCHED : GAP
skillMatch = min(currentLevel / requiredLevel, 1.0) * 100
weight = mandatory ? 2 : 1
overallMatch = sum(skillMatch * weight) / sum(weight)
```

Overall matching uses unrounded individual scores, then rounds to two decimals. Extra skills do not affect the result. Above-required proficiency is capped at 100%.

Response example:

```json
{
  "jobId": 1,
  "jobTitle": "Java Developer",
  "company": "ABC Technologies",
  "overallMatchPercent": 87.50,
  "evaluable": true,
  "mandatorySkillsMet": false,
  "skills": [
    {
      "skillId": 1,
      "skillName": "Java",
      "currentLevel": 3,
      "requiredLevel": 4,
      "gap": 1,
      "mandatory": true,
      "matchPercent": 75.00,
      "status": "GAP"
    },
    {
      "skillId": 2,
      "skillName": "MySQL",
      "currentLevel": 4,
      "requiredLevel": 3,
      "gap": 0,
      "mandatory": true,
      "matchPercent": 100.00,
      "status": "MATCHED"
    }
  ]
}
```

The supplied two-skill example labels the total 75%, but its specified formula yields **87.50%**. The backend follows the formula.

Only positive gaps produce recommendations:

| Condition | Priority |
|---|---|
| Mandatory and gap >= 2 | 1 |
| Mandatory and gap < 2 | 2 |
| Optional gap | 3 |

Recommendations sort by priority ascending, gap descending, then skill ID. They include skillId, skillName, currentLevel, requiredLevel, gap, priority, and reason. Persisted recommendations refresh transactionally when registration, skill names, student skills, or job requirements change. GET requests calculate from current inputs and never write to the database.

An unconfigured job returns 0.00%, `evaluable: false`, `mandatorySkillsMet: false`, and empty lists. Applications are still allowed with a 0.00% snapshot; this avoids adding an eligibility restriction not present in the requirements. Missing mandatory skills also do not block applying.

Application match is calculated on the server and stored at submission. Later skill changes affect live analysis but do not rewrite previous application scores. Duplicate student/job applications return 409.

Recommendation refresh visits all jobs for a changed student and all students for a changed job. This simple synchronous approach suits a small project; larger deployments should limit refresh to relevant student/job pairs.

## Validation and errors

Responses use HTTP status codes and a consistent JSON body:

```json
{ "status": 404, "message": "Job not found with id 10", "errors": {} }
```

Body validation adds field messages under `errors`. Common statuses:

- 400: malformed JSON, unknown fields, invalid email/password/levels/IDs/status.
- 401: incorrect login, absent or invalid/expired token.
- 403: wrong role or denied route.
- 404: missing entity or assignment.
- 409: duplicate email/skill/application, conflicting write, or deletion of referenced data.

Boolean `mandatory` is required. Decimal levels, level 0, and levels above 5 are rejected. All entity relationships are non-null and constrained in the database.

## Postman and smoke testing

Import `docs/SkillGap.postman_collection.json`. Run requests in order. The collection creates unique sample accounts, logs in both roles, saves tokens and IDs, and exercises the required endpoints. Tokens are collection variables; do not share populated collections with real credentials.

With the server running:

```powershell
.\scripts\smoke-test.ps1
```

The script creates uniquely named sample data, checks registration/login, role separation, user isolation, skill matching, recommendations, applications, duplicate protection, and an admin status update. It leaves its sample records in the selected database. To target another local port:

```powershell
.\scripts\smoke-test.ps1 -BaseUrl http://localhost:18080
```

## Tests and project layout

`mvnw.cmd clean verify` compiles, runs all tests, and builds the executable JAR. Automated tests use a separate H2 database in MySQL mode; they never connect to the normal MySQL database. The HTTP smoke test can be run against MySQL to verify the actual driver/schema.

Tests cover BCrypt and one-to-one registration, normal/admin login, JWT expiry/signature/role checks, 401/403 behavior, legacy-route denial, no session creation, two-user isolation, CRUD, validation, weighted matching, numeric priorities, recommendation persistence, application snapshots, duplicate applications, dashboard aggregates, and CORS.

```text
src/main/java/com/skillgap/analyzer/
  SkillGapAnalyzerApplication.java
  controller/   Auth, Admin, User, Skill, Job, Application, SkillGap controllers
  service/      Auth, User, Student, Skill, Job, SkillGap, Application,
                Recommendation, Dashboard services and response mapping
  repository/   Eight Spring Data repositories
  entity/       Eight JPA entities, Role and ApplicationStatus enums
  dto/          Validated request and response records
  security/     SecurityConfig, JwtService, JwtAuthenticationFilter,
                CustomUserDetailsService, SecurityErrorWriter
  exception/    ResourceNotFoundException, GlobalExceptionHandler
  config/       DataInitializer
```

Controller → Service → Repository keeps HTTP handling, business rules, and persistence separate. Authentication checks who the caller is; the SecurityFilterChain and role annotations decide which APIs they may access.

Framework references: [Spring Security password authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html), [JJWT usage](https://github.com/jwtk/jjwt), and [Spring Boot starters](https://docs.spring.io/spring-boot/reference/using/build-systems.html). Spring Boot 4 retains the requested `spring-boot-starter-web` as a compatibility starter; MVC test support uses its current dedicated starter.


## Parallel API verification

Run the concurrent HTTP test against a running backend:

```powershell
.\scripts\parallel-api-test.ps1 -BaseUrl http://localhost:8080 -Concurrency 8
```

The test sends 77 requests, dispatching up to eight before waiting for responses. It checks four users, concurrent reads and independent skill writes, JWT authorization, profile/application isolation, skill-gap scores, recommendations, and admin status updates. Eight simultaneous applications for the same student/job must produce exactly one 201 response and seven 409 responses. Expected validation and authorization errors are counted as successful checks.

The script creates uniquely named test records in the selected database and saves a token-free result report to `.build/parallel-api-results.json`. Use a separate MySQL database if you want to keep development data clean.

## VS Code Problems panel

The DTO imports have been reduced to the types they use. The workspace imports the Maven project in Standard mode with Java 21. The main source was also checked using the Eclipse compiler bundled with the installed Red Hat Java extension, including unused-import checks.

If old warnings remain after these file changes, run **Developer: Reload Window** from the VS Code Command Palette. If the old deleted `ApiController.java` still appears, close its editor tab and open the controllers under `com/skillgap/analyzer/controller`. For stale Red Hat Java project data, use **Java: Clean Java Language Server Workspace** and allow the project to reload.
