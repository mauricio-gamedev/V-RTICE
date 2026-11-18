# Session checkpoint

**Updated:** 2026-11-18  
**Branch:** feat/foundation-scaffold  
**Milestone:** Foundation scaffold

## Repository state

- Repository: mauricio-gamedev/V-RTICE
- Default branch: main
- Repository was empty when received.
- One bootstrap README commit was required on main to create the initial Git object/ref.
- Feature development is isolated on feat/foundation-scaffold.

## Implemented in this branch

- Product, architecture, stack, and test strategy documentation.
- Repository instructions and contribution/security guidance.
- Gradle multi-project settings and pinned version catalog.
- Android application module using Kotlin and Jetpack Compose.
- Initial four-destination app shell with explicit empty states.
- Ktor backend module with a liveness endpoint and an automated route test.
- GitHub Actions workflow for Android assembly, backend tests, and APK artifact upload.

## Not implemented yet

- Authentication, account/session storage, and profile persistence.
- PostgreSQL schema/migrations and Redis integration.
- Friends, presence, conversations, messages, Círculos, permissions, and moderation.
- WebSocket event model and delivery guarantees.
- LiveKit integration, voice calls, Clearvoice, and Spectra.
- Screen capture and other-app playback audio capture.
- Production deployment, secrets management, and signing pipeline.
- Final brand availability and final package identifier validation.

## Validation status

- Source files were committed to the feature branch.
- CI initially failed at Android SDK setup because the action attempted to install the obsolete `tools` package; the workflow now installs `platform-tools` explicitly.\n- After that correction, CI reached Gradle and exposed the AGP 9 built-in Kotlin migration; the legacy Kotlin Android plugin was removed.\n- Compose 1.12.1 required compileSdk 37, which is currently distributed as the Android 17 preview platform under a versioned package name rather than the stable `android-37` package. To keep foundation CI on a stable SDK, the Compose BOM is pinned to stable `2026.06.01` (Compose Runtime 1.11.4) and compileSdk/targetSdk remain 36.\n- A successful CI run for this updated dependency baseline has not yet been observed.
- No local Android SDK/Gradle installation was available in the authoring environment.
- Do not describe the Android build or backend test as passing until CI provides a successful run.
- The Compose screen is a navigable foundation shell, not a connected production client.

## Next task

1. Inspect GitHub Actions run for this branch and fix any build/test failures.
2. Add API configuration and a server-side health contract.
3. Add the first PostgreSQL migration and repository integration tests.
4. Design the identity/session domain and security flows before implementing authentication.
5. Implement the first vertical slice: account, profile, friend request, and direct message.

## Working rule

Read AGENTS.md and this checkpoint before continuing. Update this checkpoint after each meaningful implementation session.
