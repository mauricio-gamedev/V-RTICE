# Architecture baseline

## System boundary

The mobile app is a client, not the source of truth. The backend owns identity, relationships, community membership, roles, permissions, message persistence, moderation, and authorization to join media rooms.

The SFU handles real-time media transport. It does not own VÉRTICE social data.

    Android (Kotlin / Compose)
      - UI and design system
      - Feature modules
      - Local persistence and cache
      - HTTPS + WebSocket application connection
      - LiveKit client for media
                 |
                 v
    VÉRTICE API (Ktor)
      - Identity and sessions
      - Social graph and presence
      - Communities, channels, roles, permissions
      - Messaging and attachments
      - Moderation
      - Media-token authorization
          - PostgreSQL: durable data
          - Redis: ephemeral presence / rate limits
          - S3-compatible storage: file objects
          - FCM: push notifications

    LiveKit SFU: real-time audio/video transport

## Current repository modules

- android/app: first Compose shell and navigation.
- backend/app: Ktor service entry point and liveness check.
- docs: product requirements, architecture, decisions, and handoff checkpoint.

This is intentionally a small starting point. Do not create empty modules for every planned feature before there is a concrete implementation.

## Android layering target

As the client grows, use these boundaries:

- **UI:** Compose screens, components, navigation, and UI state.
- **Domain:** use cases and business rules independent of Android UI.
- **Data:** repositories, API contracts, persistence, and synchronization.
- **Media:** audio routing, room state, and screen capture adapters.

Add feature modules when code ownership and dependency boundaries are clear. Avoid premature module fragmentation.

## Backend modular-monolith target

Keep one deployable Ktor service initially, with independently testable packages/modules for identity, sessions, social graph, presence, communities, channels, roles, messaging, moderation, and media-token issuance.

Use PostgreSQL as durable source of truth. Redis is for expiring or high-frequency state such as presence leases and rate limits. Keep durable records out of Redis.

## Real-time contracts

Use HTTPS for commands and queries and WebSocket events for application updates. Events should have stable names, versioned payloads, timestamps, and identifiers for deduplication. Message delivery must be idempotent and support retry.

Media transport is separate. The API validates membership and permissions before issuing short-lived LiveKit room credentials. LiveKit API secrets stay on the server.

## Environment configuration

- Read runtime settings from environment variables.
- Fail fast on missing required production secrets.
- Provide safe local defaults only for non-sensitive developer settings.
- Never commit .env files, production credentials, signing keys, or SFU secrets.

## First vertical slice

The first end-to-end product slice is: create account, create profile, send/accept friend request, and exchange a direct message. It must use a real backend and server-side authorization before it is considered complete.
