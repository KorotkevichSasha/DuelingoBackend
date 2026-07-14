# Duelingo Backend

Spring Boot backend and React administration panel for Duelingo.

## Local configuration

1. Copy `.env.example` to `.env`.
2. Replace every `CHANGE_ME` value with a unique secret.
3. Generate `JWT_SIGNING_KEY` as a Base64-encoded random value of at least 32 bytes.
4. Never commit `.env`, uploaded files, database dumps, credentials, or production logs.

Production secrets must be stored in the deployment platform's environment-variable/secret storage.

## Run

```shell
docker compose up --build
```

Only the application port is intended for external access. Database, monitoring, and administration ports are bound to localhost in the provided Compose file.

## Security

Report security issues privately to the repository owner. Do not include secrets or personal data in a public issue.
