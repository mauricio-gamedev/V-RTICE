# Session checkpoint

**Updated:** 2026-11-18  
**Branch:** feat/staging-deployment  
**Milestone:** Closed staging environment and invitation-gated registration

## Repository state

- Repository: mauricio-gamedev/V-RTICE
- Default branch: main
- Foundation branch: feat/foundation-scaffold (foundation APK manually tested by the project owner)
- Backend identity branch: feat/identity-auth, PR #2 (draft, unmerged)
- Android identity branch: feat/android-auth-ui, PR #3 (draft, stacked on feat/identity-auth, unmerged)
- Current staging branch: feat/staging-deployment (based on feat/android-auth-ui; PR #4 open as a draft targeting feat/android-auth-ui)

## Validation evidence from upstream branches

- Backend CI run 44379065102 completed successfully on commit a86989b34331f25210892061969188413e7c1cf1: [run](https://github.com/mauricio-gamedev/V-RTICE/actions/runs/44379065102).
- Android CI run 44379059940 completed successfully on commit a9c6857e755d326faac54209f8de840c5c93c470: [run](https://github.com/mauricio-gamedev/V-RTICE/actions/runs/44379059940).
- Android debug APK artifact: vertice-debug-apk, artifact ID 14462532022, SHA-256 4e6b118137b18f6e94efed27db230a9290eb2e0ef93c5a7d01151809e32cbbbb.
- The two upstream PRs remain open drafts. This branch has not yet had CI validation.
- Upstream CI success does not prove a hosted API or remote signup flow.

## Existing identity implementation

- PostgreSQL-backed users and auth sessions through Flyway.
- Argon2id password hashing, short-lived HS256 access JWTs, opaque refresh tokens stored as SHA-256 digests, atomic refresh rotation, and session revocation.
- Ktor registration, login, refresh, logout, and authenticated profile routes.
- Android HTTPS-only API URL configuration, Compose login/registration UI, Android Keystore AES-GCM refresh-token storage, in-memory access token, session restoration, and logout.

## Implemented on feat/staging-deployment

- Registration request now includes an invitation code.
- Server checks the environment-provided registration code with constant-time byte comparison and rejects invalid codes with HTTP 403.
- The invitation value is not stored in the database or committed as a real secret.
- Android signup form collects the beta code and sends it to the API.
- Local Docker Compose passes REGISTRATION_INVITE_CODE and defaults the connection pool to two.
- Render Blueprint provisions a free Docker web service in Virginia, with HTTPS managed by Render, health check at /health/live, generated JWT secret, and auto-deploy after checks pass.
- Deployment guide describes Neon PostgreSQL in AWS US East 1 (N. Virginia), JDBC SSL URL formatting, dashboard secrets, and exact health-check procedure.
- CI workflow now also builds the staging Docker image to validate the Render packaging path.

## Security and operations boundaries

- Invite code is a temporary closed-beta gate, not production abuse prevention.
- Email verification, password reset/recovery, login throttling, abuse monitoring, database backup validation, and production observability are still incomplete.
- No Render or Neon resources have been created yet. No API hostname exists yet.
- Database credentials, JWT_SECRET, and REGISTRATION_INVITE_CODE must be entered only in provider dashboards or local ignored .env files. Never commit real values.
- Render free service sleeps after inactivity and is for testing, not public production.
- Do not store sensitive personal information in staging.

## Next tasks

1. Validate current branch CI, including backend tests, Android APK build, and Docker image build. Fix all failures.
2. Keep PR #4 in draft until CI is green and the deployment diff is reviewed.
3. Configure Neon and Render resources with secrets entered privately in their dashboards.
4. Use the exact generated Render HTTPS hostname in Android and test /health/live, signup, login, refresh, profile, and logout on the Galaxy A06.
5. After identity is verified, implement friend requests and Pulse presence, followed by direct messages and Círculos.

## Working rules

- Keep feature work off main and preserve the existing PR stack.
- Do not merge without passing checks and review.
- Never claim a CI result or remote device flow passed without evidence.
