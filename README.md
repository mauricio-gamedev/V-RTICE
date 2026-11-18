# VÉRTICE

**VÉRTICE** is an Android-first social communication platform designed for friends and communities. It aims to make it easy to find people, start conversations, gather in voice rooms, and build spaces with their own identity.

> VÉRTICE is a working codename. Product and brand validation are still pending.

## Product direction

- Social presence, friendships, and direct/group conversations.
- Configurable communities called Círculos.
- Voice rooms designed for low-friction drop-in.
- Screen sharing and live activities as later milestones.
- Original identity, themes, badges, roles, and moderation.
- Measured performance on modest Android devices.

## Repository layout

| Path | Responsibility |
|---|---|
| android/app | Android application (Kotlin + Jetpack Compose) |
| backend/app | Backend API (Kotlin + Ktor) |
| docs/ | Product, architecture, decisions, and checkpoints |
| .github/workflows/ | CI and build automation |

## Current state

Foundation scaffold:
- Android app shell with four navigable destinations and honest empty states.
- Ktor backend with a liveness endpoint and a route test.
- Pinned Gradle version catalog and GitHub Actions workflow.
- Product, architecture, testing, security, and contribution documentation.

Not implemented yet: accounts, persisted profiles, friendships, presence, messages, Círculos, database integration, voice calls, or screen sharing. The current app is not a connected chat client.

## Build

CI uses Gradle 9.6.0, JDK 17, and Android SDK 36.

    gradle :android:app:assembleDebug
    gradle :backend:app:test
    gradle :backend:app:run

Requirements: JDK 17 or newer, Gradle 9.6.0, and Android SDK 37 for the Android app.

## Engineering rules

- Do not commit feature work directly to main.
- Read AGENTS.md and docs/SESSION_CHECKPOINT.md before continuing.
- Follow the product specification and architecture documents.
- Keep secrets out of source code and APKs.
- Do not mark a feature complete without tests and evidence.

## Documentation

- Product specification: docs/PRODUCT_SPEC.md
- Architecture: docs/ARCHITECTURE.md
- Stack decisions: docs/engineering/STACK.md
- Test strategy: docs/TEST_STRATEGY.md
- Session checkpoint: docs/SESSION_CHECKPOINT.md
- Contributing: CONTRIBUTING.md
- Security policy: SECURITY.md
