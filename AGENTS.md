# Repository instructions

## Before making changes

1. Read this file.
2. Read docs/SESSION_CHECKPOINT.md.
3. Read the relevant product, architecture, and decision documents.
4. Inspect the existing implementation and tests before editing.

## Git workflow

- Never place feature work directly on main.
- Use focused branches: feat/<name>, fix/<name>, refactor/<name>, or docs/<name>.
- Keep commits scoped and descriptive.
- Open a pull request for integration; do not merge without passing required checks and review.
- Do not rewrite shared history or force-push unless explicitly requested.

## Product and architecture

- Preserve VÉRTICE's original identity. Do not copy another platform's screen layouts, naming, or assets.
- Keep Android UI, domain rules, data access, backend modules, and media transport separated.
- The backend is the authority for identity, membership, roles, permissions, message ownership, and media access.
- Never put database credentials, LiveKit API secrets, signing keys, or service tokens in the Android client.
- Treat presence and call state as ephemeral; do not use them as substitutes for durable database records.
- Ask for explicit user consent before microphone, camera, or screen capture.
- Do not claim universal capture of other apps' playback audio on Android.

## Android quality

- Kotlin + Jetpack Compose.
- Avoid unnecessary dependencies, global observers, heavy animations, and unbounded caches.
- Keep blocking work off the main thread.
- Support compact screens and provide accessible labels and contrast.
- Validate runtime behavior on a modest device, with the Galaxy A06 as an early reference device.

## Backend quality

- Validate authorization on the server for every protected operation.
- Use explicit request/response contracts and versioned database migrations.
- Validate all user-controlled input and uploaded content.
- Add rate limits to authentication, invitations, and message creation before public beta.
- Keep health/liveness checks separate from dependency readiness checks.

## Validation and handoff

- Run relevant unit, integration, lint, and build tasks.
- Fix regressions before opening a PR.
- Update docs and docs/SESSION_CHECKPOINT.md with implementation state, exact tests run, known gaps, and next task.
- Never report a build or test as passing unless there is evidence from a completed command or CI run.
