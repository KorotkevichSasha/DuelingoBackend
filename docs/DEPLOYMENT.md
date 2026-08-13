# Production deployment

## Required infrastructure

- One HTTPS hostname for the API behind a trusted TLS certificate.
- PostgreSQL 15+, MongoDB 8.2+, and Redis 8.8+ with backups and private networking. Never downgrade a database image against an existing data volume.
- Persistent storage mounted at `/app/uploads`, or a future migration of avatars to object storage.
- SMTP credentials for transactional email.

## Required secrets

Set every variable from `.env.example`. In production, `APP_PUBLIC_BASE_URL` must be the public HTTPS API origin and `ALLOWED_ORIGINS` must list only the real administration-panel origin. Generate `JWT_SIGNING_KEY` from at least 32 cryptographically random bytes encoded as Base64. Never reuse database, Redis, SMTP, Grafana, or JWT credentials.

## Rollout

1. Run `./gradlew test bootJar` and build the container image.
2. Back up PostgreSQL, MongoDB, Redis, and uploaded avatars.
3. Deploy the image. Liquibase applies forward-only database migrations on startup.
4. Wait for `/actuator/health` to report `UP` before routing traffic.
5. Smoke-test registration, login, refresh, avatar upload, profile, learning, duel WebSocket, and account deletion.
6. Verify logs and metrics, then keep the previous image available for rollback. Database rollback requires restoring the backup; do not edit an already-applied Liquibase changeset.

## Operations

- Expose only HTTPS ports publicly. PostgreSQL, MongoDB, Redis, Grafana, Prometheus, and Mongo Express must remain private.
- Enable daily encrypted backups and perform a restore drill before launch.
- Configure uptime monitoring for `/actuator/health`, log retention, disk alerts, and certificate-expiry alerts.
- Rotate secrets after any suspected exposure. Rotating the JWT signing key signs users out.
- The included Render blueprint uses a paid persistent disk because avatar files must survive restarts. For horizontal scaling, replace local avatar storage with S3-compatible object storage first.
