# ADR-0002: Server-owned identity and rotating refresh sessions

- **Status:** Accepted
- **Date:** 2026-11-18

## Context

VÉRTICE needs account identity that can safely authorize messages, friendships, communities, and media-room access. The mobile client must not become the authority for a user's identity or permissions.

## Decision

- Store users and sessions in PostgreSQL.
- Hash passwords with Argon2id.
- Issue short-lived HMAC-signed JWT access tokens containing user and session identifiers.
- Issue opaque random refresh tokens and store only SHA-256 hashes.
- Rotate refresh tokens atomically on every successful refresh.
- Check that the associated session is active during JWT authentication, allowing logout/revocation to take effect immediately.
- Apply schema changes only through versioned Flyway migrations.

## Consequences

- Refresh-token replay is rejected after a successful rotation.
- Revocation currently adds a database lookup to each authenticated request. This is acceptable for the initial stage; optimize with a carefully invalidated cache only after measurement.
- JWT signing keys must be unique per environment, at least 32 bytes, and managed outside source control.
- Email verification, password reset, rate limiting, and broader account recovery remain required before public signup.
