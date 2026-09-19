# SportSync backend

SportSync is a Spring Boot backend for sports-centre bookings, facility and equipment management, partner matching, notifications, and user authentication.

## Requirements

- Java 17
- MySQL
- Redis
- An SMTP account
- A Google OAuth client ID

## Configuration

Runtime credentials must be supplied through environment variables. Copy the names from `.env.example`, replace every placeholder locally, and export them before starting the application. Do not commit a populated `.env` file or credentials in Spring configuration files.

The most important required values are:

- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATA_REDIS_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `SPRING_MAIL_PASSWORD` and `SPRING_MAIL_FROM`

`JWT_SECRET` must be a securely generated Base64-encoded value containing at least 32 random bytes.

## Run locally

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Build

```bash
./mvnw clean verify
```

## Security

If a credential is committed accidentally, revoke or rotate it immediately before removing it from Git history. History cleanup alone does not make an exposed credential safe again.
