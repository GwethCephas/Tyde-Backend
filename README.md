# Tyde Backend

Spring Boot + Kotlin backend for Tyde, an AI-powered job application assistant. Provides the REST API consumed by the Android client, which authenticates via Firebase.

## Stack

- Kotlin, Spring Boot, Gradle Kotlin DSL, Java 21
- PostgreSQL, Spring Data JPA, Flyway
- Spring Security with Firebase Admin SDK token verification

## Getting started

1. Copy `.env.example` to `.env` and fill in local values (see that file for required variables).
2. Provide the environment variables to the run configuration (IntelliJ: Run Configuration → Environment variables).
3. Run a local PostgreSQL instance matching `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`.
4. `./gradlew bootRun`

## Build and test

```
./gradlew build
./gradlew test
```

Integration tests use Testcontainers and require a running Docker daemon.
