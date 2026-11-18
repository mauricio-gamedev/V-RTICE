# ADR-0001: Start with a modular monorepo

- **Status:** Accepted
- **Date:** 2026-11-18

## Context

The Android client, backend contracts, and product documentation will evolve together. The project is early-stage and has no operational need for separate repositories or microservices.

## Decision

Start with one repository containing the Android application, the Kotlin/Ktor backend, and documentation. Keep Android and backend Gradle projects independently buildable. Start the backend as a modular monolith, not a fleet of microservices.

## Consequences

- Shared product changes and API contracts can be reviewed together.
- One CI workflow can validate both client and backend.
- Clear module boundaries are still required to prevent accidental coupling.
- Split repositories or services only when a measured team, deployment, or scaling constraint justifies it.
