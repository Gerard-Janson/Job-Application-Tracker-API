# Job Application Tracker API

A RESTful backend for tracking your job search. Register an account, log in, and manage every application you submit, from the first time you save a posting through to an accepted offer. Each user only ever sees and modifies their own data.

Built with **Java 21**, **Spring Boot**, **Spring Security (JWT)**, **Spring Data JPA** and **PostgreSQL**.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Authentication](#authentication)
- [API Reference](#api-reference)
- [Error Handling](#error-handling)
- [Running Tests](#running-tests)
- [Project Structure](#project-structure)
- [Known Limitations](#known-limitations)
- [License](#license)

---

## Features

- **Stateless JWT authentication** with sign-up and sign-in endpoints
- **BCrypt password hashing**
- **Full CRUD** for job applications, plus a dedicated status-update endpoint
- **Per-user data isolation**: every query is scoped to the authenticated user, and accessing another user's record returns `404` rather than leaking its existence
- **Request validation** with Jakarta Bean Validation (field lengths, required fields, date rules)
- **Sensible defaults**: applications created without a status start as `SAVED`
- **UUID v7 primary keys** for time-sortable, non-guessable identifiers
- **CORS** pre-configured for a local frontend on `http://localhost:3000`
- **Integration tests** covering authentication and ownership rules

## Tech Stack

| Area        | Technology                                  |
|-------------|---------------------------------------------|
| Language    | Java 21                                     |
| Framework   | Spring Boot 4.1.1 (Web MVC)                 |
| Security    | Spring Security, JJWT 0.12.6                |
| Persistence | Spring Data JPA / Hibernate, PostgreSQL     |
| Validation  | Jakarta Bean Validation                     |
| Build       | Maven (wrapper included)                    |
| Testing     | JUnit 5, MockMvc, H2 (in-memory)            |
| Utilities   | Lombok                                      |

---

## Getting Started

### Prerequisites

- **JDK 21** or newer
- **PostgreSQL** (running locally or reachable over the network)
- Maven is optional, since the project ships with the Maven wrapper (`mvnw` / `mvnw.cmd`)

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/Job-Application-Tracker-API.git
cd Job-Application-Tracker-API
```

### 2. Create the database

```sql
CREATE DATABASE jobtracker;
```

Tables are created and updated automatically on startup (`spring.jpa.hibernate.ddl-auto=update`).

### 3. Set environment variables

The app reads its secrets from the environment. There are no defaults for the database credentials or the JWT secret, so the app will not start without them.

**macOS / Linux**

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=your_db_password
export JWT_SECRET=$(openssl rand -base64 48)
```

**Windows (PowerShell)**

```powershell
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your_db_password"
$env:JWT_SECRET  = "a-long-random-string-of-at-least-32-characters"
```

> `JWT_SECRET` is used as an HMAC key and must be at least 32 characters long (256 bits). Keep it private and never commit it.

### 4. Run the application

```bash
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`. The API is now available at `http://localhost:8080`.

### 5. Try it out

```bash
# Register
curl -X POST http://localhost:8080/api/v1/tracker/auth/sign-up \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"supersecret123"}'

# Sign in and capture the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/tracker/auth/sign-in \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"supersecret123"}' | jq -r .accessToken)

# Create an application
curl -X POST http://localhost:8080/api/v1/tracker/ \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "position": "Backend Engineer",
        "company": "Acme Corp",
        "jobType": "FULL_TIME",
        "workMode": "REMOTE",
        "salary": 85000,
        "status": "APPLIED"
      }'

# List your applications
curl http://localhost:8080/api/v1/tracker/ -H "Authorization: Bearer $TOKEN"
```

### Build a runnable JAR

```bash
./mvnw clean package
java -jar target/jobapplicationtracker-0.0.1-SNAPSHOT.jar
```

---


## Authentication

The API uses stateless **Bearer token** authentication.

1. Create an account with `POST /api/v1/tracker/auth/sign-up`.
2. Exchange your credentials for a token with `POST /api/v1/tracker/auth/sign-in`.
3. Send the token on every other request:

```
Authorization: Bearer <accessToken>
```

Tokens expire after 15 minutes by default (see `expiresIn` in the sign-in response, in seconds). Once a token expires, sign in again. Only the `/api/v1/tracker/auth/**` endpoints are public; everything else requires a valid token.

---

## API Reference

**Base URL:** `http://localhost:8080/api/v1/tracker`

### Auth endpoints

#### `POST /auth/sign-up`

Create a new account. Emails are trimmed and lower-cased before being stored.

```json
{
  "email": "you@example.com",
  "password": "supersecret123"
}
```

| Field      | Rules                              |
|------------|------------------------------------|
| `email`    | Required, valid email format       |
| `password` | Required, 8 to 72 characters       |

**Responses:** `201 Created` (empty body), `400 Bad Request` (validation), `409 Conflict` (email already registered)

#### `POST /auth/sign-in`

```json
{
  "email": "you@example.com",
  "password": "supersecret123"
}
```

**Response `200 OK`**

```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Other responses:** `400 Bad Request` (validation), `401 Unauthorized` (bad credentials)

### Job application endpoints

All endpoints below require the `Authorization` header.

| Method   | Endpoint        | Description                           | Success          |
|----------|-----------------|---------------------------------------|------------------|
| `GET`    | `/`             | List all of your applications         | `200 OK`         |
| `POST`   | `/`             | Create an application                 | `201 Created`    |
| `GET`    | `/{id}`         | Get one application                   | `200 OK`         |
| `PUT`    | `/{id}`         | Replace an application's details      | `200 OK`         |
| `PATCH`  | `/{id}/status`  | Update only the status                | `200 OK`         |
| `DELETE` | `/{id}`         | Delete an application                 | `204 No Content` |

`{id}` is a UUID. An unknown id, or an id belonging to another user, returns `404 Not Found`.

#### Create / update request body (`POST /`, `PUT /{id}`)

```json
{
  "position": "Backend Engineer",
  "company": "Acme Corp",
  "jobDescription": "Build and maintain REST services.",
  "location": "Cape Town, South Africa",
  "jobType": "FULL_TIME",
  "workMode": "HYBRID",
  "salary": 85000,
  "postUrl": "https://acme.example/careers/backend-engineer",
  "status": "APPLIED",
  "appliedDate": "2026-10-01",
  "nextFollowUpDate": "2026-10-15"
}
```
> **`PUT` semantics:** the request body overwrites all fields. Optional fields you omit are set to `null`. The one exception is `status`, which is left unchanged if omitted.

#### Update status (`PATCH /{id}/status`)

```json
{
  "status": "INTERVIEW",
  "note": "Phone screen scheduled for Friday"
}
```

`status` is required. `note` is optional (max 500 characters) and is currently accepted but not stored (see [Known Limitations](#known-limitations)).

#### Response body

```json
{
  "id": "0199e5a2-7c3b-7d1e-9f4a-2b8c6d5e1a30",
  "position": "Backend Engineer",
  "company": "Acme Corp",
  "jobDescription": "Build and maintain REST services.",
  "location": "Cape Town, South Africa",
  "jobType": "FULL_TIME",
  "workNode": "HYBRID",
  "salary": 85000,
  "post_url": "https://acme.example/careers/backend-engineer",
  "status": "APPLIED",
  "appliedDate": "2026-10-01",
  "nextFollowUpDate": "2026-10-15",
  "createdAt": "2026-10-19",
  "updatedAt": "2026-10-19"
}
```

> Note that the response currently uses the field names `workNode` (not `workMode`) and `post_url` (not `postUrl`). See [Known Limitations](#known-limitations).


## Error Handling

| Status                     | When                                                                |
|----------------------------|---------------------------------------------------------------------|
| `400 Bad Request`          | Validation failed (missing required field, bad format, etc.)        |
| `401 Unauthorized`         | Missing, invalid or expired token, or wrong sign-in credentials     |
| `404 Not Found`            | Application doesn't exist or belongs to another user                |
| `409 Conflict`             | Email already registered                                            |

---

## Running Tests

```bash
./mvnw test
```

Tests run against an in-memory **H2** database using `src/test/resources/application.properties`, so no PostgreSQL instance or environment variables are needed. The suite includes:

- A context-load smoke test
- Integration tests (`JobApplicationOwnershipTest`) verifying that unauthenticated requests are rejected, users only see their own applications, a missing status defaults to `SAVED`, and invalid requests return `400`

---

## Project Structure

```
src/main/java/com/gerard/jobapplicationtracker
├── controller/    REST endpoints (AuthController, JobApplicationController)
├── service/       Business logic and user lookup for Spring Security
├── repository/    Spring Data JPA repositories
├── entity/        JPA entities (User, JobApplication)
├── dtos/          Request and response records
├── mapper/        Entity <-> DTO mapping
├── enums/         Status, Job_type, Work_mode
└── security/      JWT utilities, auth filter, security and CORS config
```

---

## Known Limitations

These are worth knowing about, and make good candidates for future improvements:

- **Response field naming:** responses expose `workNode` and `post_url` instead of `workMode` and `postUrl`, so they don't match the request body.
- **Status notes aren't persisted:** `PATCH /{id}/status` validates the `note` field but does not save it, and there is no status history yet.
- **CORS** only allows `http://localhost:3000`. Update `SecurityConfig` for other frontends or production origins.
- **No refresh tokens:** users must sign in again after the 15-minute token expires.
- **No pagination, filtering or sorting** on the list endpoint.
- **Schema management** uses Hibernate `ddl-auto=update`; a migration tool such as Flyway or Liquibase is recommended for production.
- **SQL logging** (`spring.jpa.show-sql=true`) is enabled by default and should be turned off in production.

---

## License

Distributed under the MIT License. See [`LICENSE`](LICENSE) for details.

Copyright (c) 2026 Gerard Janson