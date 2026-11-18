# Session checkpoint

**Updated:** 2026-11-18  
**Branch:** feat/identity-auth  
**Milestone:** Identity and authentication — first implementation

## Repository state

- Repository: mauricio-gamedev/V-RTICE
- Default branch: main
- Foundation branch: feat/foundation-scaffold
- Current implementation branch: feat/identity-auth (based on feat/foundation-scaffold)
- The foundation APK was downloaded and manually tested by the project owner.
- The foundation PR remains separate; do not merge or rewrite its history from this feature branch.

## Implemented on feat/identity-auth

- Identity domain records and repository contract.
- Argon2id password hashing service (64 MiB memory, 3 iterations, 1 lane).
- HS256 JWT access tokens with 15-minute expiry.
- Opaque 256-bit refresh tokens, SHA-256 token digests, 30-day rolling session expiry, and atomic refresh rotation.
- PostgreSQL JDBC repository backed by HikariCP.
- Flyway migration for users and auth_sessions tables.
- Ktor routes: register, login, refresh, logout, and current profile.
- Server-side session validation on protected requests.
- Unit/API tests using a fake identity repository and a PostgreSQL integration test activated by TEST_DATABASE_URL.
- Dockerfile and docker-compose setup for a local PostgreSQL + API environment.
- Identity API documentation and ADR-0002.

## Not implemented yet

- Android sign-up/login/profile screens connected to this backend.
- A hosted backend URL, TLS, deployment secrets, and production database.
- Email verification, password reset, account recovery, and authentication rate limiting.
- Friendships, presence, direct/group messaging, Círculos, permissions, and moderation.
- WebSocket event model and delivery guarantees.
- LiveKit integration, voice calls, Clearvoice, Spectra, and screen capture.
- Final brand availability and final package identifier validation.

## Security and deployment constraints

- Do not expose signup publicly until HTTPS, email verification, abuse/rate limits, and operational monitoring are enabled.
- JWT_SECRET must be unique per environment and at least 32 UTF-8 bytes.
- Never place JWT_SECRET, database credentials, or other backend secrets in the Android client or APK.
- Refresh tokens are single-use and are invalidated when rotated or when the server-side session is revoked.

## Validation status

- Foundation APK was manually tested by the project owner.
- Identity branch CI has been configured to compile the Android app, run backend/API tests, and run a PostgreSQL-backed integration test against a PostgreSQL service.
- The identity branch CI result has not yet been confirmed.
- No production deployment has been created.
- Do not describe identity features as production-ready until CI, API deployment, TLS, and device integration tests pass.

## Next task

1. Inspect and fix the first identity-branch CI run.
2. Add Android registration/login screens and repository/ViewModel.
3. Define a secure API base URL per build variant; do not invent a production hostname.
4. Deploy the API/database to a chosen host with HTTPS and secrets kept server-side.
5. Run registration/login/profile/refresh/logout end-to-end from the physical Android app.
6. Implement friend requests and presence on top of the now-defined account identity.

## Working rule

Read AGENTS.md and this checkpoint before continuing. Update this checkpoint after each meaningful implementation session.
