# DuelRush Backend

Spring Boot backend and React administration panel for DuelRush.

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

On Windows, the local launcher also waits for a healthy API, starts the admin panel,
restores USB forwarding for every connected Android device, and opens the installed app:

```powershell
.\scripts\start-duelrush-local.ps1
```

You can also double-click `start-duelrush-local.bat`.

Use `-RebuildBackend -RebuildAndroid -InstallAndroid` after changing both projects.

Only the application port is intended for external access. Database, monitoring, and administration ports are bound to localhost in the provided Compose file.

## Security

Report security issues privately to the repository owner. Do not include secrets or personal data in a public issue.

## Release documentation

- See `docs/DEPLOYMENT.md` for server configuration and rollout checks.
- See the Android repository's `PLAY_RELEASE_CHECKLIST.md` before creating a Play Console release.
