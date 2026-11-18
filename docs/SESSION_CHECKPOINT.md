# Session checkpoint

**Updated:** 2026-11-18  
**Branch:** feat/android-auth-ui  
**Milestone:** Android identity and authenticated app entry

## Repository state

- Repository: mauricio-gamedev/V-RTICE
- Default branch: main
- Foundation branch: feat/foundation-scaffold (foundation APK downloaded and manually tested by the project owner)
- Backend identity branch: feat/identity-auth, PR #2 (draft)
- Current Android integration branch: feat/android-auth-ui, PR #3 (draft, stacked on feat/identity-auth)

## Implemented in this branch

- Ktor Android client for identity endpoints.
- HTTPS-only configurable API base URL, saved as a non-secret app preference.
- API liveness check before login or registration.
- Login and registration screen in Portuguese.
- Auth response/user DTOs aligned with backend contracts.
- Refresh token encrypted using Android Keystore AES-256-GCM; ciphertext and IV stored in private SharedPreferences.
- Access JWT held only in runtime memory, not persisted to disk.
- Session restore by rotating the saved refresh token at app launch.
- Profile screen populated from the authenticated API response.
- Logout calls the backend and revokes the session.
- Transient network failures do not erase the encrypted refresh token; an invalid/revoked refresh token clears it.

## Backend identity dependency

The upstream feat/identity-auth branch provides:
- PostgreSQL users and auth_sessions tables through Flyway.
- Argon2id password hashing.
- Short-lived HS256 access JWTs.
- Opaque refresh tokens with SHA-256 digests and atomic rotation.
- Ktor registration, login, refresh, logout, and /v1/me routes.
- API tests and a PostgreSQL integration test in CI.

## Not implemented yet

- Hosted HTTPS API or production database.
- Email verification, password reset, account recovery, authentication rate limits, abuse monitoring, and deployment monitoring.
- Friendship requests, presence, direct/group messaging, Círculos, channels, permissions, moderation, and notification delivery.
- WebSocket events and LiveKit media integration.
- Voice rooms, Clearvoice processing, Spectra screen sharing, or camera video.
- Final product brand availability validation.

## Configuration and security

- The app intentionally does not contain a hard-coded API hostname.
- Configure a real HTTPS base URL in the login screen. The UI refuses HTTP URLs.
- Do not use a production endpoint until HTTPS, email verification, abuse controls, and account recovery are deployed.
- Database credentials and JWT signing secret remain on the backend and must never be included in the APK.
- Access JWTs are only in memory; refresh tokens are encrypted at rest with an Android Keystore key.
- If the device cannot decrypt the saved refresh token, the local session is cleared and the user must sign in again.

## Validation status

- Foundation APK was manually installed and tested by the project owner.
- Earlier identity CI runs exposed compile errors in Ktor StatusPages imports, Argon2Factory enum references, and Ktor client contentType imports. These were corrected on both feature branches.
- CI for latest identity branch commit and latest Android UI commit is currently running; do not mark them successful until the current runs finish.
- No APK from the new authentication UI branch has yet been verified by a completed successful CI run.
- No API deployment exists, so end-to-end account creation against a remote server has not been tested on the physical device.

## Next task

1. Inspect the latest CI runs for feat/identity-auth and feat/android-auth-ui; fix any remaining compile/test issues.
2. Keep PR #2 based on feat/foundation-scaffold and PR #3 based on feat/identity-auth; merge in that dependency order after successful checks.
3. Select and configure a backend hosting target with HTTPS and managed secrets; do not invent a hostname.
4. Test signup/login/refresh/profile/logout on the Galaxy A06 against the deployed API.
5. Implement friend requests and the Pulse presence model next.
6. Continue with direct messaging and Círculos after the social identity layer is verified.

## Working rule

Read AGENTS.md and this checkpoint before continuing. Update this checkpoint after each meaningful implementation session.
