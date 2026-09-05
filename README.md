# Stark Fusion

Employee and student skill gap analyzer with a Spring Boot backend and React frontend.

## Backend

Java 21, Spring Boot, MySQL, Spring Security, JWT, and BCrypt. Includes admin/user APIs for skills, jobs, applications, skill-gap analysis, recommendations, and dashboard information.

See [backend setup and API documentation](Backend/README.md), the [Postman collection](Backend/docs/SkillGap.postman_collection.json), and the [backend verification report](Backend/docs/VERIFICATION.md).

## Frontend

See [frontend setup](frontend/README.md) for the React application and API configuration. Copy `frontend/.env.example` to your local environment file when configuring the API URL.

## Development

Start the MySQL-backed API using the backend instructions, then run the frontend using its package scripts. Development credentials and signing-key defaults in the backend are for local use; configure private values before deployment.

Generated build output, installed dependencies, local environment files, and database files are excluded from version control.
